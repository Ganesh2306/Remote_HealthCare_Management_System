package com.example.demo.Service;

import com.example.demo.Models.Admin;
import com.example.demo.Models.Appointment;
import com.example.demo.Models.Doctor;
import com.example.demo.Models.Doctor.Qualification;
import com.example.demo.Models.User;
import com.example.demo.dto.DoctorRegistrationRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.demo.Models.User.EmergencyContact;
import com.example.demo.dto.DoctorDTO;
import com.example.demo.respository.AdminRepository;
import com.example.demo.respository.AppointmentRepository;
import com.example.demo.respository.DoctorRepository;
import jakarta.persistence.EntityNotFoundException;
import com.example.demo.Exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class DoctorService {

    private static final Logger log = LoggerFactory.getLogger(DoctorService.class);

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private EmailService emailService;


    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    public List<Doctor> findAllDoctors() {
        return doctorRepository.findByIsActiveTrue();
    }

    public List<DoctorDTO> getAllAvailableDoctors() {
        return findAllDoctors().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private DoctorDTO convertToDTO(Doctor doctor) {
        DoctorDTO dto = new DoctorDTO();
        dto.setUserId(doctor.getUserId());
        dto.setFirstName(doctor.getFirstName());
        dto.setLastName(doctor.getLastName());
        dto.setSpecialty(doctor.getSpecialization());
        return dto;
    }

    public Doctor findByUserId(String id) {
        return doctorRepository.findByUserId(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));
    }

    public Page<Doctor> getAllDoctors(int page, int size) {
        if (page < 1 || size < 1) {
            throw new IllegalArgumentException("Page number and size must be greater than 0");
        }
        Pageable pageable = PageRequest.of(page - 1, size);
        return doctorRepository.findAll(pageable);
    }





    public void deactivateDoctor(String userId, String reason) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with userId: " + userId));

        handleDoctorAppointments(doctor);
        doctor.deactivate();
        sendDeletionNotification(doctor, reason);
    }


 public Page<Doctor> searchDoctors(String query, Pageable pageable) {
        return doctorRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query, query, pageable);
    }



    private void handleDoctorAppointments(Doctor doctor) {
        List<Appointment> futureAppointments = appointmentRepository
                .findByDoctorAndStartTimeAfter(doctor, LocalDateTime.now());

        for (Appointment appointment : futureAppointments) {
            appointment.setStatus(Appointment.Status.CANCELLED);
            appointment.setCancellationReason("Doctor no longer available");
            appointmentRepository.save(appointment);

            emailService.sendAppointmentCancellationNotification(
                    appointment.getPatient().getEmail(),
                    appointment.getPatient().getFirstName(),
                    doctor.getFullName(),
                    appointment.getStartTime(),
                    appointment.getReason()
            );
        }
    }

   

    private void sendDeletionNotification(Doctor doctor, String reason) {
        try {
        

            if (doctor.getEmail() != null) {
                emailService.sendDoctorAccountDeactivationEmail(doctor.getEmail(), doctor.getFullName(), reason);
            }
        } catch (Exception e) {
            log.error("Failed to send deletion notifications for doctor {}: {}", doctor.getUserId(), e.getMessage());
        }
    }


    public void registerDoctor(DoctorRegistrationRequest request, Admin admin) {
    // 1. Check if email already exists
    if (userService.emailExists(request.getEmail())) {
        throw new IllegalArgumentException("Email already registered");
    }

    // Create and save the Doctor
    Doctor doctor = new Doctor();
    
    // Basic Information
    doctor.setFirstName(request.getFirstName());
    doctor.setLastName(request.getLastName());
    doctor.setEmail(request.getEmail());
    doctor.setPassword(passwordEncoder.encode(request.getPassword()));
    doctor.setRole(User.Role.DOCTOR); // Changed from ADMIN to DOCTOR
    doctor.reactivate();

    // Personal Information
    doctor.setDateOfBirth(request.getDateOfBirth());
    doctor.setGender(request.getGender());

    // Health Information
    doctor.setBloodType(request.getBloodType());
    
    // Contact Information
    doctor.setPhoneNumber(request.getPhoneNumber());
    doctor.setAddress(request.getAddress());

    // Professional Information
    doctor.setLicenseNumber(request.getLicenseNumber());
    doctor.setSpecialization(request.getSpecialization()); // Added specialization
    
    // Emergency Contact Information
    if (doctor.getEmergencyContact() == null) {
        doctor.setEmergencyContact(new EmergencyContact());
    }
    doctor.getEmergencyContact().setName(request.getEmergencyContact().getName());
    doctor.getEmergencyContact().setPhoneNumber(request.getEmergencyContact().getPhone());
    doctor.getEmergencyContact().setEmail(request.getEmergencyContact().getEmail());
    doctor.getEmergencyContact().setRelationship(request.getEmergencyContact().getRelationship());

   // Handle Qualifications
if (request.getQualifications() != null && !request.getQualifications().isEmpty()) {
    Set<Qualification> qualifications = request.getQualifications().stream()
        .map(q -> {
            Qualification qualification = new Qualification();
            qualification.setName(q.getName());
            qualification.setObtainedDate(q.getObtainedDate());
            qualification.setInstitution(q.getInstitution());
            return qualification;
        })
        .collect(Collectors.toSet());
    doctor.setQualifications(new ArrayList<>(qualifications));
}
    // Save the doctor
    doctor = doctorRepository.save(doctor);
    
    // Add to admin's managed doctors
    admin.getManagedDoctors().add(doctor);
    adminRepository.save(admin);

    // Send verification email
    emailService.sendRegistrationSuccessEmail(
        doctor.getEmail(), 
        doctor.getFullName(), 
        doctor.getUserId(), 
        request.getPassword()
    );
}
  
  
    
public void reactivateDoctor(String DoctorId, String reason, Admin admin){
   
    Doctor doctor = doctorRepository.findByUserId(DoctorId)
    .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + DoctorId));

adminRepository.save(admin);
doctor.reactivate();
emailService.sendAccountReactivationEmail(doctor.getEmail(), doctor.getFullName(), reason, LocalDateTime.now());
}
}
