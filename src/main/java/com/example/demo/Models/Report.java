package com.example.demo.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "vital_signs_id")
    private VitalSigns vitalSigns; // Foreign Key to VitalSigns (ManyToOne)

    @ManyToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment; // Foreign Key to Appointment (nullable)

    @ManyToOne
    @JoinColumn(name = "alert_id")
    private Alert alert;

 @ManyToOne(cascade = CascadeType.ALL)
@JoinColumn(name = "prescription_id")
private Prescription prescription;

@ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;  // <-- New field for Patient

@ManyToOne(cascade = CascadeType.ALL)
@JoinColumn(name = "feedback_id")
private Feedback feedback;


    @PrePersist
    @PreUpdate
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructors, Getters, Setters

    public Report() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public VitalSigns getVitalSigns() {
        return vitalSigns;
    }

    public void setVitalSigns(VitalSigns vitalSigns) {
        this.vitalSigns = vitalSigns;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
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



    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }


    public Alert getAlert(){
        return this.alert;
    }

    public void setAlert(Alert alert){
        this.alert=alert;
    }
}
