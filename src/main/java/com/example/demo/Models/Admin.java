package com.example.demo.Models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "Admin")
@PrimaryKeyJoinColumn(name = "user_id")
public class Admin extends User {

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "admin_managed_doctors",
        joinColumns = @JoinColumn(name = "admin_id", referencedColumnName = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "doctor_id", referencedColumnName = "user_id")
    )
    private Set<Doctor> managedDoctors = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "admin_managed_patients",
        joinColumns = @JoinColumn(name = "admin_id", referencedColumnName = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "patient_id", referencedColumnName = "user_id")
    )
    private Set<Patient> managedPatients = new HashSet<>();

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SystemLog> systemLogs = new ArrayList<>();

    public Admin() {
        // Default constructor required by JPA
    }

    public Admin(String userId, String firstName, String lastName, String email,
                 String password, LocalDate dateOfBirth, String address,
                 String phoneNumber, Gender gender, String nationality,
                 EmergencyContact emergencyContact, BloodType bloodType,
                 String identificationNumber) {

        super(firstName, lastName, email, Role.ADMIN, password, dateOfBirth,
              address, phoneNumber, gender,  emergencyContact, bloodType);
    }

    // Getters and Setters
    public Set<Doctor> getManagedDoctors() {
        return managedDoctors;
    }

    public void setManagedDoctors(Set<Doctor> managedDoctors) {
        this.managedDoctors = managedDoctors;
    }

    public Set<Patient> getManagedPatients() {
        return managedPatients;
    }

    public void setManagedPatients(Set<Patient> managedPatients) {
        this.managedPatients = managedPatients;
    }

    public List<SystemLog> getSystemLogs() {
        return systemLogs;
    }

    public void setSystemLogs(List<SystemLog> systemLogs) {
        this.systemLogs = systemLogs;
    }

    @Override
    public void displayInfo() {
        System.out.printf("Admin Info:\n" +
            "User ID: %s\n" +
            "Name: %s %s\n" +
            "Email: %s\n" +
            "Phone: %s\n" +
            "Address: %s\n" +
            "Role: %s\n" +
            "Managed Doctors: %d\n" +
            "Managed Patients: %d\n",
            getUserId(), getFirstName(), getLastName(), getEmail(), getPhoneNumber(),
            getAddress(), getRole(), managedDoctors.size(), managedPatients.size());
    }
}


