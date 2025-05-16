package com.example.demo.Service;

import com.example.demo.Models.Patient;
import com.example.demo.Models.Admin;
import com.example.demo.Models.VitalSigns;
import com.example.demo.Models.SystemLog;
import com.example.demo.respository.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final SystemLogService logService;
    private final AdminRepository adminRepository;
    private final AppointmentRepository appointmentRepository;
      private final VitalSignsRepository vitalSignsRepository;
    
    private final EmailService emailService;

    public PatientService(PatientRepository patientRepository, SystemLogService logService,
    AppointmentRepository appointmentRepository, AdminRepository adminRepository, EmailService emailService,
     VitalSignsRepository vitalSignsRepository) {
        this.patientRepository = patientRepository;
        this.logService = logService;
        this.adminRepository=adminRepository;
        this.appointmentRepository=appointmentRepository;
        this.emailService=emailService;
         this.vitalSignsRepository=vitalSignsRepository;
    }

    






    public int getPendingVitalsRequestsCount(String doctorId) {
        return vitalSignsRepository.countByDoctorUserIdAndStatus(
            doctorId, 
            VitalSigns.Status.REQUESTED
        );
    }

   public int getPatientCountForDoctor(String doctorId) {
    // Get distinct patient IDs from both sources
    Set<String> appointmentPatientIds = appointmentRepository.findDistinctConfirmedPatientIdsByDoctorUserId(doctorId);
    Set<String> vitalSignPatientIds = vitalSignsRepository.findDistinctPatientIdsByDoctorUserIdAndStatus(doctorId, VitalSigns.Status.REQUESTED);
    
    // Combine and count unique patients
    Set<String> allPatientIds = new HashSet<>();
    allPatientIds.addAll(appointmentPatientIds);
    allPatientIds.addAll(vitalSignPatientIds);
    
    return allPatientIds.size();
}
    public Page<Patient> searchPatients(String query, Pageable pageable) {
        return patientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query, query, pageable);
    }

    
    
    
    
    
    
    
    
    
    
    
    
    public Page<Patient> getAllPatients(Pageable pageable) {
        return patientRepository.findAll(pageable);
    }


      
   @Transactional
public void deactivatePatient(String patientId, Admin admin,String reason) {
    // 1. First fetch the patient with all relationships
    Patient patient = patientRepository.findById(patientId)
        .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + patientId));

    adminRepository.save(admin);

    // 4. Delete all related entities
    appointmentRepository.deleteByPatientUserId(patientId); // Delete appointments
    
    // 5. Finally deactivate the patient
   patient.deactivate();

    // 6. Create log
    logService.createLog(admin, "Deactivated patient: " + patient.getFullName(), 
                       SystemLog.Severity.WARNING);

    // 7. Send deletion notification email
   emailService.sendAccountDeactivationEmail(patient.getEmail(), patient.getFullName(), reason, LocalDateTime.now());
}

public void reactivatePatient(String patientId, Admin admin, String reason){
    Patient patient = patientRepository.findById(patientId)
    .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + patientId));


// 3. Remove patient from admin's managed patients
adminRepository.save(admin);
patient.reactivate();;
logService.createLog(admin,  "Reactivated Patient: "+patient.getFullName(),  SystemLog.Severity.WARNING);
emailService.sendAccountReactivationEmail(patient.getEmail(), patient.getFullName(), reason, LocalDateTime.now());
}



}