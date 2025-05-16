package com.example.demo.dto;

import com.example.demo.Models.Doctor;

public class DoctorDTO {
    private String userId;
    private String firstName;
    private String lastName;
    private Doctor.Specialization specialty;

    // Add these convenience methods for Thymeleaf
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getSpecialization() {
        return specialty != null ? specialty.name() : "";
    }

    // Existing getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

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

    public Doctor.Specialization getSpecialty() {
        return specialty;
    }

    public void setSpecialty(Doctor.Specialization specialty) {
        this.specialty = specialty;
    }
}