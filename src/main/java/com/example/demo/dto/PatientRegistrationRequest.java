package com.example.demo.dto;

import com.example.demo.Models.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class PatientRegistrationRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$", 
             message = "Password must be at least 8 characters with at least 1 letter, 1 number and 1 special character")
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private User.Gender gender;

    @NotNull(message = "Blood Type is required")
    private User.BloodType bloodType;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9\\s-]{10,}$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    private String address;

    @Valid
    @NotNull(message = "Emergency contact is required")
    private EmergencyContactDTO emergencyContact = new EmergencyContactDTO();

    @AssertTrue(message = "You must accept the terms and conditions")
    private boolean termsAgreement;

    // --- Inner DTO Class ---
    public static class EmergencyContactDTO {
        @NotBlank(message = "Emergency contact name is required")
        private String name;

        @NotBlank(message = "Emergency contact phone is required")
        @Pattern(regexp = "^\\+?[0-9\\s-]{10,}$", message = "Invalid phone number format")
        private String phone;

        @Email(message = "Emergency contact email should be valid")
        private String email;

        @NotBlank(message = "Emergency contact relationship is required")
        private String relationship;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getRelationship() {
            return relationship;
        }

        public void setRelationship(String relationship) {
            this.relationship = relationship;
        }
    }

    // --- Getters and Setters for outer DTO ---
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public User.Gender getGender() {
        return gender;
    }
    public void setGender(User.Gender gender) {
        this.gender = gender;
    }

    public User.BloodType getBloodType() {
        return bloodType;
    }
    public void setBloodType(User.BloodType bloodType) {
        this.bloodType = bloodType;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public EmergencyContactDTO getEmergencyContact() {
        return emergencyContact;
    }
    public void setEmergencyContact(EmergencyContactDTO emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public boolean isTermsAgreement() {
        return termsAgreement;
    }
    public void setTermsAgreement(boolean termsAgreement) {
        this.termsAgreement = termsAgreement;
    }
}
