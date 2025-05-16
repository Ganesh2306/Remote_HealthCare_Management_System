package com.example.demo.Models;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "User")
public abstract class User {

    public enum Role { ADMIN, DOCTOR, PATIENT }
    public enum Gender { MALE, FEMALE, OTHER }
    public enum BloodType {
        A_POSITIVE, A_NEGATIVE, B_POSITIVE, B_NEGATIVE,
        AB_POSITIVE, AB_NEGATIVE, O_POSITIVE, O_NEGATIVE
    }

    @Id
    @Column(nullable = false, updatable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private String passwordHash;

    private LocalDate dateOfBirth;
    private String address;
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private BloodType bloodType;

    private boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;

    @Entity
    @Table(name = "emergency_contacts")
    public static class EmergencyContact {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false)
        private String name;

        @Column(nullable = false)
        private String phoneNumber;

        @Column(nullable = false)
        private String relationship;

        @Column
        private String email;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id")
        @JsonBackReference
        private User user;

        public EmergencyContact() {}

        public EmergencyContact(String name, String phoneNumber, String relationship, String email) {
            this.name = name;
            this.phoneNumber = phoneNumber;
            this.relationship = relationship;
            this.email = email;
        }

        // Getters and setters
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getRelationship() { return relationship; }
        public String getEmail() { return email; }
        
        @JsonIgnore
        public User getUser() { return user; }

        public void setName(String name) { this.name = name; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public void setRelationship(String relationship) { this.relationship = relationship; }
        public void setEmail(String email) { this.email = email; }
        public void setUser(User user) { this.user = user; }
    }

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "emergency_contact_id")
    @JsonManagedReference
    private EmergencyContact emergencyContact;

    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z-'\\s]{2,50}$");

    protected User() {
        this.userId = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    protected User(String firstName, String lastName, String email, Role role,
                 String password, LocalDate dateOfBirth, String address, String phoneNumber,
                 Gender gender, EmergencyContact emergencyContact, BloodType bloodType) {
        this();
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        setRole(role);
        setPassword(password);
        setDateOfBirth(dateOfBirth);
        setAddress(address);
        setPhoneNumber(phoneNumber);
        setGender(gender);
        setEmergencyContact(emergencyContact);
        setBloodType(bloodType);
    }

    public void setPassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }
        this.passwordHash = password; // Service layer will encode before saving
        updateTimestamp();
    }

    public void recordLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    protected static String validate(String input, Pattern pattern, String errorMessage) {
        if (input == null || !pattern.matcher(input).matches()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return input;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public boolean isEnabled() { return isActive; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    
    @JsonIgnore
    public String getPassword() { return passwordHash; }
    
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getAddress() { return address; }
    public String getPhoneNumber() { return phoneNumber; }
    public Gender getGender() { return gender; }
    public EmergencyContact getEmergencyContact() { return emergencyContact; }
    public BloodType getBloodType() { return bloodType; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getLastLogin() { return lastLogin; }

    public void setFirstName(String firstName) {
        this.firstName = validate(firstName, NAME_PATTERN, "Invalid first name");
        updateTimestamp();
    }

    public void setLastName(String lastName) {
        this.lastName = validate(lastName, NAME_PATTERN, "Invalid last name");
        updateTimestamp();
    }

    public void setEmail(String email) {
        this.email = validate(email, EMAIL_PATTERN, "Invalid email format");
        updateTimestamp();
    }

    public void setRole(Role role) {
        this.role = Objects.requireNonNull(role, "Role cannot be null");
        updateTimestamp();
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        updateTimestamp();
    }

    public void setAddress(String address) {
        this.address = address;
        updateTimestamp();
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        updateTimestamp();
    }

    public void setGender(Gender gender) {
        this.gender = gender;
        updateTimestamp();
    }

    public void setEmergencyContact(EmergencyContact emergencyContact) {
        this.emergencyContact = emergencyContact;
        if (emergencyContact != null) {
            emergencyContact.setUser(this);
        }
        updateTimestamp();
    }

    public void setBloodType(BloodType bloodType) {
        this.bloodType = Objects.requireNonNull(bloodType, "Blood type cannot be null");
        updateTimestamp();
    }

    public void deactivate() {
        this.isActive = false;
        updateTimestamp();
    }

    public void reactivate() {
        this.isActive = true;
        updateTimestamp();
    }

    // Spring Security integration
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    public abstract void displayInfo();
}