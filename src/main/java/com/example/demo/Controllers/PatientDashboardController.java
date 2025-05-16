package com.example.demo.Controllers;


import com.example.demo.Models.Admin;
import com.example.demo.Models.Alert;
import com.example.demo.Models.Doctor;
import com.example.demo.Models.Patient;
import com.example.demo.Models.SystemLog;
import com.example.demo.Service.UserService;
import com.example.demo.Service.VitalSignsService;
import com.example.demo.dto.CSVUploadResponse;
import com.example.demo.dto.DashboardVitalsDto;
import com.example.demo.dto.DoctorDTO;
import com.example.demo.dto.VitalTrendsDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import com.example.demo.Service.SystemLogService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.respository.DoctorRepository;
import com.example.demo.respository.PatientRepository;

import com.example.demo.Service.DoctorService;
import com.example.demo.Service.EmailService;

import java.util.Map;

import com.example.demo.Service.AlertService;
import com.example.demo.Service.AppointmentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("/patient/dashboard")
public class PatientDashboardController {


    
    private final VitalSignsService vitalSignsService;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final DoctorService doctorService;
    private final PatientRepository patientRepository;
    private final SystemLogService logService;
    @Autowired
    private AlertService alertService;
    
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private EmailService emailService;


    public PatientDashboardController(
            VitalSignsService vitalSignsService,
            UserService userService,
            ObjectMapper objectMapper, DoctorService doctorService,
            AppointmentService appointmentService,PatientRepository patientRepository,
            SystemLogService logService) {
        this.vitalSignsService = vitalSignsService;
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.doctorService=doctorService;
        this.patientRepository=patientRepository;
        this.logService=logService;
    }

 @GetMapping
public String showDashboard(Model model, Authentication authentication) {
    final Logger logger = LoggerFactory.getLogger(PatientDashboardController.class);
    logger.info("Entering showDashboard method");

    try {
        // 1. Get authenticated user
        logger.debug("Getting authenticated user email");
        String email = authentication.getName();
        logger.debug("Authenticated email: {}", email);

        // 2. Find patient
        logger.debug("Looking up patient by email: {}", email);
        Patient patient = (Patient) userService.findByEmail(email);
        
        if (patient == null) {
            logger.error("Patient not found for email: {}", email);
            throw new RuntimeException("Patient not found");
        }
        logger.debug("Found patient: {}", patient.getUserId());

        // 3. Get dashboard data
        logger.debug("Fetching dashboard data for patient ID: {}", patient.getUserId());
        DashboardVitalsDto dashboardData = vitalSignsService.getDashboardData(patient.getUserId());
        
        // Initialize default values if null
        if (dashboardData == null) {
            logger.warn("Dashboard data is null, initializing empty DTO");
            dashboardData = new DashboardVitalsDto();
        }
        
        if (dashboardData.getTrends() == null) {
            logger.debug("Trends data is null, initializing empty trends");
            dashboardData.setTrends(new VitalTrendsDto());
        }

        // 4. Prepare JSON data for charts
        logger.debug("Preparing trends JSON data");
        String trendsJson = "{}";
        try {
            VitalTrendsDto vitalTrends = dashboardData.getTrends();
            if (vitalTrends != null) {
                logger.debug("Checking for non-null trend components");
                boolean hasData = vitalTrends.getTimestamps() != null || 
                                vitalTrends.getHeartRates() != null ||
                                vitalTrends.getSystolicBP() != null ||
                                vitalTrends.getDiastolicBP() != null ||
                                vitalTrends.getOxygenLevels() != null ||
                                vitalTrends.getTemperatures() != null ||
                                vitalTrends.getRespiratoryRates() != null;
                
                if (hasData) {
                    logger.debug("Serializing trends data to JSON");
                    trendsJson = objectMapper.writeValueAsString(Map.of(
                        "timestamps", vitalTrends.getTimestamps() != null ? vitalTrends.getTimestamps() : List.of(),
                        "heartRates", vitalTrends.getHeartRates() != null ? vitalTrends.getHeartRates() : List.of(),
                        "systolicBP", vitalTrends.getSystolicBP() != null ? vitalTrends.getSystolicBP() : List.of(),
                        "diastolicBP", vitalTrends.getDiastolicBP() != null ? vitalTrends.getDiastolicBP() : List.of(),
                        "oxygenLevels", vitalTrends.getOxygenLevels() != null ? vitalTrends.getOxygenLevels() : List.of(),
                        "temperatures", vitalTrends.getTemperatures() != null ? vitalTrends.getTemperatures() : List.of(),
                        "respiratoryRates", vitalTrends.getRespiratoryRates() != null ? vitalTrends.getRespiratoryRates() : List.of()
                    ));
                    logger.trace("Generated trends JSON: {}", trendsJson);
                } else {
                    logger.debug("No trend data available");
                }
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize trends data", e);
            trendsJson = "{}";
        }

           List<DoctorDTO> doctorDTOs = doctorService.getAllAvailableDoctors();


        // 5. Add model attributes
        logger.debug("Adding model attributes");
        model.addAttribute("patient", patient);
        model.addAttribute("dashboardData", dashboardData);
        model.addAttribute("trendsJson", trendsJson);
        model.addAttribute("doctors", doctorDTOs);
        boolean hasVitalData = dashboardData.isHasData();
        boolean hasAlerts = dashboardData.hasAlerts();
        model.addAttribute("hasVitalData", hasVitalData);
        model.addAttribute("hasAlerts", hasAlerts);
        
        logger.debug("Model attributes added - hasVitalData: {}, hasAlerts: {}", hasVitalData, hasAlerts);

        logger.info("Successfully processed dashboard request");
        return "patient/dashboard";
    } catch (Exception e) {
        logger.error("Error in dashboard controller", e);
        throw e;
    }

}


  // Show upload form
    @GetMapping("/uploadVital/csv")
    public String showUploadForm(Model model) {
       
    
          List<DoctorDTO> doctorDTOs = doctorService.getAllAvailableDoctors();
    model.addAttribute("doctors", doctorDTOs);
        return "patient/VitalUpload";
    }


    // Handle CSV upload
    @PostMapping("/uploadVital/csv")
    @ResponseBody
    public ResponseEntity<CSVUploadResponse> uploadVitalsCSV(
            @RequestParam("csvFile") MultipartFile file,
            @RequestParam("doctorId") String doctorId,  // <-- Add this line
            Authentication authentication) {
    

      

        if (file.isEmpty()) {
          
            return ResponseEntity.badRequest().body(
                    new CSVUploadResponse(false, 0, 0, 1,
                            "Empty file",
                            List.of("The uploaded file is empty"),
                            file.getOriginalFilename())
            );
        }

        try {
            String email = authentication.getName();
            Optional<Patient> patient1 = patientRepository.findByEmail(email);
            Patient patient=patient1.get();
            String patientId = patient.getUserId();
                    Optional<Admin> admin = userService.findAdminForUser(patient);

            CSVUploadResponse response = vitalSignsService.processCSV(file, patientId,doctorId, admin.get());

            if (response.isSuccess()) {
               
                logService.createLog(admin.get(), "vitals Uploaded:"+ patientId, SystemLog.Severity.INFO);
                return ResponseEntity.ok(response);
            } else {
                   logService.createLog(admin.get(), "vitals Failed:"+ patientId, SystemLog.Severity.ERROR);
                return ResponseEntity.unprocessableEntity().body(response);
            }

        } catch (Exception e) {

            return ResponseEntity.internalServerError().body(
                    new CSVUploadResponse(false, 0, 0, 1,
                            "Internal server error",
                            List.of("Unexpected error: " + e.getMessage()),
                            file.getOriginalFilename())
            );
        }
    }

    // Provide downloadable CSV template
    @GetMapping("/templates/VitalSignsCSV.csv")
    public ResponseEntity<Resource> downloadTemplate() {
        try {
            Resource resource = vitalSignsService.getCSVTemplate();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"VitalSignsTemplate.csv\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);
        } catch (Exception e) {
           
            return ResponseEntity.internalServerError().build();
        }
    }


    
    @PostMapping("/alert/emergency")
    public ResponseEntity<?> createEmergencyAlert(
            @RequestBody EmergencyAlertRequest request,
            Authentication authentication) {
        
        try {
            Patient patient = patientRepository.findByUserId(request.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
            
            Doctor doctor = doctorRepository.findByUserId(request.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

            Alert alert = new Alert();
            alert.setPatient(patient);
            alert.setDoctor(doctor);
            alert.setMessage(request.getMessage());
            alert.setCategory(request.getCategory());
            alert.setTimestamp(LocalDateTime.now());
            alert.setAcknowledged(false);
            
            alertService.createAlert(alert);
            
            // Send notification to selected doctor
            emailService.sendEmergencyAlertToDoctor(
                doctor.getEmail(),
                patient.getFullName(),
                request.getMessage(),
                request.getCategory()
            );
            emailService.sendEmergencyAlertSentConfirmation(patient.getEmail(),doctor.getFullName(),
            patient.getFullName(),request.getMessage(), request.getCategory());


            emailService.sendEmergencyContactAlert(patient.getEmergencyContact().getEmail(), patient.getFullName(),
             request.getMessage(), request.getCategory());
            
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    public static class EmergencyAlertRequest {
        private String patientId;
        private String doctorId;
        private String category;
        private String message;
        
        // Getters and setters
        public String getPatientId() { return patientId; }
        public void setPatientId(String patientId) { this.patientId = patientId; }
        public String getDoctorId() { return doctorId; }
        public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}

