package com.example.demo.Service;

import com.example.demo.Models.*;
import com.example.demo.Models.User.EmergencyContact;
import com.example.demo.dto.AdminRegistrationRequest;
import com.example.demo.respository.AdminRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@Transactional
public class AdminRegistrationService {

    private final AdminRepository adminRepository;
    private final UserService userService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public AdminRegistrationService(
           AdminRepository adminRepository,
            UserService userService,
            EmailService emailService,
            PasswordEncoder passwordEncoder) {
        this.adminRepository=adminRepository;
        this.userService = userService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerAdmin(AdminRegistrationRequest request) {
        // 1. Check if email already exists
        if (userService.emailExists(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

      // Create and save the Patient
      
      Admin admin = new Admin();
// Basic Information
admin.setFirstName(request.getFirstName());
admin.setLastName(request.getLastName());
admin.setEmail(request.getEmail());
admin.setPassword(passwordEncoder.encode(request.getPassword()));
admin.setRole(User.Role.ADMIN);
admin.reactivate();

// Personal Information
admin.setDateOfBirth(request.getDateOfBirth());
admin.setGender(request.getGender());

// Health Information
admin.setBloodType(request.getBloodType());
// Contact Information
admin.setPhoneNumber(request.getPhoneNumber());
admin.setAddress(request.getAddress());

    if (admin.getEmergencyContact() == null) {
        admin.setEmergencyContact(new EmergencyContact());
    }

// Emergency Contact Information
admin.getEmergencyContact().setName(request.getEmergencyContact().getName());
admin.getEmergencyContact().setPhoneNumber(request.getEmergencyContact().getPhone());
admin.getEmergencyContact().setEmail(request.getEmergencyContact().getEmail());
admin.getEmergencyContact().setRelationship(request.getEmergencyContact().getRelationship());

// Save the admin
adminRepository.save(admin); // Save the admin
        
admin = adminRepository.save(admin);

        // 4. Send verification email

   emailService.sendRegistrationSuccessEmail(admin.getEmail(), admin.getFullName(), admin.getUserId(), request.getPassword());
    }
}
