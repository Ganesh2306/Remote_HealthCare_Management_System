package com.example.demo.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Patient patient;

    @ManyToOne
    private Doctor doctor;

    private String message;

    private LocalDateTime timestamp;

    private boolean acknowledged;

    private String category; // MEDICATION, APPOINTMENT, VITAL_SIGN, TEST_RESULT, etc.

    // 🔗 Optional link to Prescription
    @ManyToOne
    private Prescription prescription;

    // 🔗 Optional link to Feedback
    @ManyToOne
    private Feedback feedback;

    // Constructors
    public Alert() {
    }

    public Alert(Long id, Patient patient, Doctor doctor, String message,
                 LocalDateTime timestamp, boolean acknowledged, String category,
                 Prescription prescription, Feedback feedback) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.message = message;
        this.timestamp = timestamp;
        this.acknowledged = acknowledged;
        this.category = category;
        this.prescription = prescription;
        this.feedback = feedback;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Prescription getPrescription() {
        return prescription;
    }

    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public void setFeedback(Feedback feedback) {
        this.feedback = feedback;
    }

  
}
