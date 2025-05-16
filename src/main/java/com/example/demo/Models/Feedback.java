package com.example.demo.Models;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public  Feedback() {}

    public Feedback(
                    Doctor doctor, Patient patient, String comments) {
      
        this.doctor = doctor;
        this.patient = patient;
        this.comments = comments;
        this.createdAt = LocalDateTime.now();
    }

    // Add validation in service to enforce: only one of appointment or vitalUpload should be non-null

    // Getters
    public Long getId() { return id; }
 
    public Doctor getDoctor() { return doctor; }
    public Patient getPatient() { return patient; }
    public String getComments() { return comments; }
    public LocalDateTime getCreatedAt() { return createdAt; }


    public void setDoctor(Doctor doctor){
        this.doctor=doctor;
    }

    public void setPatient(Patient patient){
        this.patient=patient;
    }

    public void setCreatedAt(LocalDateTime localDateTime){
        this.createdAt=localDateTime;
    }

   
    public void setComments(String comments) { this.comments = comments; }
}

