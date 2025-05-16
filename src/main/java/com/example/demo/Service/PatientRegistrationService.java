package com.example.demo.Service;

import com.example.demo.Models.*;
import com.example.demo.Models.User.EmergencyContact;
import com.example.demo.dto.PatientRegistrationRequest;
import com.example.demo.respository.PatientRepository;
import com.example.demo.respository.VerificationTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PatientRegistrationService {

    private final PatientRepository patientRepository;
    private final UserService userService;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AdminService adminService;

    public PatientRegistrationService(
            PatientRepository patientRepository,
            UserService userService,
            VerificationTokenRepository tokenRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder, 
            AdminService adminService) {
        this.patientRepository = patientRepository;
        this.userService = userService;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.adminService = adminService;
    }

    public void registerPatient(PatientRegistrationRequest request) {
        // 1. Check if email already exists
        if (userService.emailExists(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Create and save the Patient
        Patient patient = new Patient();

        // Basic Information
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setEmail(request.getEmail());
        patient.setPassword(passwordEncoder.encode(request.getPassword()));
        patient.setRole(User.Role.PATIENT);
        patient.deactivate(); // Disabled until email verification

        // Personal Information
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());

        // Health Information
        patient.setBloodType(request.getBloodType());
        
        // Contact Information
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setAddress(request.getAddress());

        // Emergency Contact Information
        EmergencyContact emergencyContact = new EmergencyContact();
        emergencyContact.setName(request.getEmergencyContact().getName());
        emergencyContact.setPhoneNumber(request.getEmergencyContact().getPhone());
        emergencyContact.setEmail(request.getEmergencyContact().getEmail());
        emergencyContact.setRelationship(request.getEmergencyContact().getRelationship());
        patient.setEmergencyContact(emergencyContact);

        // Save the patient first to generate ID
        patient = patientRepository.save(patient);
        
        // Assign patient to admin
            adminService.assignPatientToAdmin(patient);
        

        // Generate verification token
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setUser(patient);
        verificationToken.setEmailType(VerificationToken.EmailType.VERIFICATION);
        tokenRepository.save(verificationToken);

        // Send verification email
        emailService.sendEmail(request, verificationToken.getToken(), VerificationToken.EmailType.VERIFICATION);
    }
}