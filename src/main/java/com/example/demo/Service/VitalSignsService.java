package com.example.demo.Service;

import com.example.demo.Models.*;
import com.example.demo.Exceptions.CSVParseException;
import org.springframework.data.domain.Page;
import com.example.demo.dto.*;
import com.example.demo.respository.*;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class VitalSignsService {

    private static final Logger logger = LoggerFactory.getLogger(VitalSignsService.class);
    private static final int TREND_DAYS = 7;
    private static final int MAX_ALERTS = 5;

    private final CSVProcessingService csvProcessingService;
    private final PatientRepository patientRepository;
    private final VitalSignsRepository vitalSignsRepository;
    private final DoctorRepository doctorRepository;
    private final EmailService emailService;
    private final SystemLogService logService;

    @Autowired
    public VitalSignsService(
        CSVProcessingService csvProcessingService,
        PatientRepository patientRepository,
        VitalSignsRepository vitalSignsRepository,
        DoctorRepository doctorRepository,
        EmailService emailService,
        SystemLogService logService
    ) {
        this.csvProcessingService = csvProcessingService;
        this.patientRepository = patientRepository;
        this.vitalSignsRepository = vitalSignsRepository;
        this.doctorRepository = doctorRepository;
        this.emailService = emailService;
        this.logService = logService;
    }

    @Transactional
    public CSVUploadResponse processCSV(MultipartFile file, String patientId, String doctorId, Admin admin) {
        if (file == null || file.isEmpty()) {
            return createErrorResponse("No file provided", file);
        }

        Patient patient = patientRepository.findByUserId(patientId)
            .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        Doctor doctor = doctorRepository.findByUserId(doctorId)
            .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        try {
            List<VitalSigns> vitals = csvProcessingService.parseCSV(file.getInputStream());
            List<String> validationErrors = new ArrayList<>();
            List<VitalSigns> validRecords = new ArrayList<>();
            List<VitalSigns> criticalRecords = new ArrayList<>();
           List< List<String>> alerts=new ArrayList<>();
            for (int i = 0; i < vitals.size(); i++) {
                try {
                    VitalSigns vital = vitals.get(i);
                    vital.setPatient(patient);
                    vital.setDoctor(doctor);
                    validateVitalSigns(vital);

                    boolean critical = isCriticalCondition(vital);
                            if (critical) {
                                logger.info("Critical vital detected: {}", vital);
                                criticalRecords.add(vital);
                            }   

                    validRecords.add(vital);
                    alerts.add(checkForAlerts(vital));
                    
                } catch (Exception e) {
                    validationErrors.add(String.format("Row %d: %s", i + 1, e.getMessage()));
                    logger.warn("CSV validation error at row {}: {}", i + 1, e.getMessage());
                }
            }

            if (!validationErrors.isEmpty() && validRecords.isEmpty()) {
                return new CSVUploadResponse(false, vitals.size(), 0, validationErrors.size(),
                        "All records failed validation", validationErrors, file.getOriginalFilename());
            }

            List<VitalSigns> savedRecords = vitalSignsRepository.saveAll(validRecords);

            if (!savedRecords.isEmpty()) {
                try {
                    emailService.sendVitalsUploadConfirmation(patient.getEmail(), savedRecords.size());
                    emailService.sendDoctorVitalsNotification(doctor.getEmail(), patient.getFullName(), savedRecords.size());

                    if (!criticalRecords.isEmpty()) {
                        
                        emailService.sendCriticalConditionAlert(patient.getEmail(), criticalRecords.size(),alerts);
                        emailService.sendCriticalConditionDoctorAlert(doctor.getEmail(), patient.getFullName(), criticalRecords.size(), alerts);
                       emailService.sendCriticalVitalsEmergencyContactAlert(patient.getEmergencyContact().getEmail(), patient.getEmergencyContact().getName(), patient.getFullName(), alerts, LocalDateTime.now());
                        logService.createLog(admin, "Critical Vitals Detected "+ patientId, SystemLog.Severity.CRITICAL);
                    }
                } catch (Exception e) {
                    logger.error("Failed to send notifications: {}", e.getMessage(), e);
                }
            }

            return new CSVUploadResponse(
                !savedRecords.isEmpty(),
                vitals.size(),
                savedRecords.size(),
                validationErrors.size(),
                String.format("Processed %d/%d records successfully", savedRecords.size(), vitals.size()),
                validationErrors,
                file.getOriginalFilename()
            );

        } catch (CSVParseException e) {
            logger.error("CSV parsing error: {}", e.getMessage());
            return createErrorResponse("CSV parsing error: " + e.getMessage(), file);
        } catch (IOException e) {
            logger.error("File processing error: {}", e.getMessage());
            return createErrorResponse("File processing error: " + e.getMessage(), file);
        } catch (Exception e) {
            logger.error("Unexpected error processing CSV: {}", e.getMessage(), e);
            return createErrorResponse("Unexpected error: " + e.getMessage(), file);
        }
    }

  public DashboardVitalsDto getDashboardData(String patientId) {
    logger.info("Starting dashboard data retrieval for patient: {}", patientId);
    DashboardVitalsDto dashboardData = new DashboardVitalsDto();
    
    // Get total count first (more efficient)
    int vitalsCount = vitalSignsRepository.countByPatient_UserId(patientId);
    logger.debug("Total vitals count for patient {}: {}", patientId, vitalsCount);
    dashboardData.setVitalsCount(vitalsCount);
    
    // If no vitals exist, return early
    if (vitalsCount == 0) {
        logger.info("No vitals found for patient: {}", patientId);
        dashboardData.setHasData(false);
        return dashboardData;
    }
    
    // Get latest vital
    Optional<VitalSigns> latestVitalOpt = vitalSignsRepository.findTopByPatient_UserIdOrderByTimestampDesc(patientId);
    if (latestVitalOpt.isEmpty()) {
        logger.error("Data inconsistency: Count shows {} vitals but none found for patient {}", vitalsCount, patientId);
        throw new IllegalStateException("No vitals found despite count > 0");
    }
    
    VitalSigns latestVital = latestVitalOpt.get();
    logger.debug("Latest vital found - ID: {}, Timestamp: {}", latestVital.getId(), latestVital.getTimestamp());
    dashboardData.setLatestVitals(latestVital);
    dashboardData.setAlerts(checkForAlerts(latestVital));
    dashboardData.setHasData(true);
    
    // Only look for previous vital if we have more than 1 record
    if (vitalsCount > 1) {
        logger.debug("Multiple vitals exist (count={}), searching for previous vital...", vitalsCount);
        
        Optional<VitalSigns> previousVitalOpt = vitalSignsRepository.findPreviousVital(
                patientId, 
                latestVital.getId()
        );
        
        if (previousVitalOpt.isPresent()) {
            VitalSigns previousVital = previousVitalOpt.get();
            logger.debug("Previous vital found - ID: {}, Timestamp: {}", 
                        previousVital.getId(), previousVital.getTimestamp());
            dashboardData.setPreviousVitals(previousVital);
        } else {
            logger.warn("No previous vital found despite count={}. Latest timestamp: {}, Latest ID: {}", 
                       vitalsCount, latestVital.getTimestamp(), latestVital.getId());
            
            // Additional diagnostic logging
            List<VitalSigns> allVitals = vitalSignsRepository.findByPatient_UserIdOrderByTimestampDescIdDesc(patientId);
            logger.debug("All vitals for patient {} ({} records):", patientId, allVitals.size());
            allVitals.forEach(v -> logger.debug(" - ID: {}, Timestamp: {}", v.getId(), v.getTimestamp()));
        }
    } else {
        logger.debug("Only one vital exists, skipping previous vital search");
    }
    
    // Get trend data
    LocalDateTime startDate = LocalDateTime.now().minusDays(TREND_DAYS);
    logger.debug("Fetching trend data since {}", startDate);
    List<VitalSigns> trendData = vitalSignsRepository.findTrendData(patientId, startDate);
    logger.debug("Found {} trend records", trendData.size());
    dashboardData.setTrends(processTrendData(trendData));
    
    logger.info("Completed dashboard data retrieval for patient {}", patientId);
    return dashboardData;
}

    private VitalTrendsDto processTrendData(List<VitalSigns> vitals) {
        VitalTrendsDto trends = new VitalTrendsDto();

        for (VitalSigns vital : vitals) {
            String timestampStr = vital.getTimestamp().toString();
            trends.getTimestamps().add(timestampStr);
            trends.getHeartRates().add(vital.getPulseRate());
            
            if (vital.getBloodPressure() != null) {
                trends.getSystolicBP().add(vital.getBloodPressure().getSystolic());
                trends.getDiastolicBP().add(vital.getBloodPressure().getDiastolic());
            }
            
            trends.getOxygenLevels().add(vital.getOxygenSaturation());
            trends.getTemperatures().add(vital.getBodyTemperature());
        }

        return trends;
    }

    private List<String> checkForAlerts(VitalSigns vital) {
        List<String> alerts = new ArrayList<>();

        if (vital.getBloodPressure() != null) {
            if (vital.getBloodPressure().getSystolic() > 140) {
                alerts.add("High systolic blood pressure (" + vital.getBloodPressure().getSystolic() + " mmHg)");
            }
            if (vital.getBloodPressure().getDiastolic() > 90) {
                alerts.add("High diastolic blood pressure (" + vital.getBloodPressure().getDiastolic() + " mmHg)");
            }
        }

        if (vital.getPulseRate() > 100) {
            alerts.add("Elevated heart rate (" + vital.getPulseRate() + " bpm)");
        } else if (vital.getPulseRate() < 50) {
            alerts.add("Low heart rate (" + vital.getPulseRate() + " bpm)");
        }

        if (vital.getOxygenSaturation() < 92) {
            alerts.add(String.format("Low oxygen saturation (%.1f%%)", vital.getOxygenSaturation()));
        }

        if (vital.getBodyTemperature() > 38) {
            alerts.add(String.format("High temperature (%.1f°C)", vital.getBodyTemperature()));
        } else if (vital.getBodyTemperature() < 36) {
            alerts.add(String.format("Low temperature (%.1f°C)", vital.getBodyTemperature()));
        }

        if (vital.getPainLevel() != null &&
            vital.getPainLevel().ordinal() >= VitalSigns.PainLevel.SEVERE.ordinal()) {
            alerts.add("Severe pain reported");
        }

        return alerts.size() > MAX_ALERTS ? alerts.subList(0, MAX_ALERTS) : alerts;
    }

   private boolean isCriticalCondition(VitalSigns vital) {
    if (vital == null) {
        logger.warn("Null vital signs object received");
        return false;
    }

    boolean isCritical = false;
    
    // Blood Pressure Check
    if (vital.getBloodPressure() != null) {
        int systolic = vital.getBloodPressure().getSystolic();
        int diastolic = vital.getBloodPressure().getDiastolic();
        
        if (systolic > 180 || systolic < 90) {
            logger.warn("Critical systolic BP: {}", systolic);
            isCritical = true;
        }
        if (diastolic > 120 || diastolic < 60) {
            logger.warn("Critical diastolic BP: {}", diastolic);
            isCritical = true;
        }
    } else {
        logger.debug("No blood pressure data available");
    }

    // Pulse Rate Check
    if (vital.getPulseRate() > 120 || vital.getPulseRate() < 50) {
        logger.warn("Critical pulse rate: {}", vital.getPulseRate());
        isCritical = true;
    }

    // Temperature Check
    if (vital.getBodyTemperature() > 39.0 || vital.getBodyTemperature() < 35.0) {
        logger.warn("Critical body temperature: {}", vital.getBodyTemperature());
        isCritical = true;
    }

    // Oxygen Saturation Check
    if (vital.getOxygenSaturation() < 90) {
        logger.warn("Critical oxygen saturation: {}", vital.getOxygenSaturation());
        isCritical = true;
    }

    return isCritical;
}

    public Resource getCSVTemplate() {
        return new ClassPathResource("static/templates/VitalSignsCSV.csv");
    }

    public boolean hasRecordedVitals(String patientId) {
        return vitalSignsRepository.existsByPatient_UserId(patientId);
    }

    private CSVUploadResponse createErrorResponse(String message, MultipartFile file) {
        return new CSVUploadResponse(
            false,
            0,
            0,
            1,
            message,
            List.of(message),
            file != null ? file.getOriginalFilename() : null
        );
    }

    private void validateVitalSigns(VitalSigns vital) {
        if (vital.getBloodPressure() == null) {
            throw new IllegalArgumentException("Blood Pressure must be specified");
        }
        if (vital.getBloodPressure().getSystolic() <= vital.getBloodPressure().getDiastolic()) {
            throw new IllegalArgumentException("Systolic BP must be greater than diastolic");
        }

        validateRange("Body Temperature", vital.getBodyTemperature(), 32.0, 42.0, "°C");
        validateRange("Pulse Rate", vital.getPulseRate(), 30, 200, "bpm");
        validateRange("Oxygen Saturation", vital.getOxygenSaturation(), 70.0, 100.0, "%");

        if (vital.getHeight() != null) {
            validateRange("Height", vital.getHeight(), 50, 250, "cm");
        }
        if (vital.getWeight() != null) {
            validateRange("Weight", vital.getWeight(), 1.0, 300.0, "kg");
        }
        if (vital.getPainLevel() == null) {
            throw new IllegalArgumentException("Pain Level must be specified");
        }
    }

    private void validateRange(String fieldName, double value, double min, double max, String unit) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(String.format("%s (%.1f %s) is outside safe range (%.1f-%.1f %s)",
                    fieldName, value, unit, min, max, unit));
        }
    }

    private void validateRange(String fieldName, int value, int min, int max, String unit) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(String.format("%s (%d %s) is outside safe range (%d-%d %s)",
                    fieldName, value, unit, min, max, unit));
        }
    }



    public List<VitalSigns> getRequestedVitalsForDoctor(Doctor doctor) {
        
        
        return vitalSignsRepository.findByStatusAndDoctorOrderByTimestampDesc(
            VitalSigns.Status.REQUESTED, doctor);
    }

    public Page<VitalSigns> getRequestedVitalsForDoctor(Doctor doctor, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
    return vitalSignsRepository.findByDoctorAndStatus(doctor, VitalSigns.Status.REQUESTED, pageable);
}

    
    public VitalSigns markAsReviewed(Long vitalId) throws EntityNotFoundException {
        VitalSigns vitalSigns = vitalSignsRepository.findById(vitalId)
                .orElseThrow(() -> new EntityNotFoundException("Vital signs not found"));
        
        if (vitalSigns.getPrescription() == null || vitalSigns.getFeedback() == null) {
            throw new IllegalStateException("Cannot mark as reviewed without prescription and feedback");
        }
        
        vitalSigns.setStatus(VitalSigns.Status.REVIEWED);
       return vitalSignsRepository.save(vitalSigns);
    }
    

       public VitalSigns UpdatePrescriptionStatus(Long id, Prescription prescription){

        VitalSigns vitalSigns= vitalSignsRepository.findById(id).orElseThrow(()-> new IllegalArgumentException( "Not Found Vital for Id"));
        vitalSigns.setPrescription(prescription);
        return vitalSignsRepository.save(vitalSigns);
    }

    
    
    public VitalSigns UpdateFeedbackStatus(Long id, Feedback feedback){

        VitalSigns vitalSigns= vitalSignsRepository.findById(id).orElseThrow(()-> new IllegalArgumentException( "Not Found Vital for Id"));
        vitalSigns.setFeedback(feedback);
        return vitalSignsRepository.save(vitalSigns);
    }
}