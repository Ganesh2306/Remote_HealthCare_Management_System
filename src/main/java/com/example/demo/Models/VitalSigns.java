package com.example.demo.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class VitalSigns {

    public enum Status {
        REQUESTED,   // Not yet reviewed by a doctor
        REVIEWED     // Reviewed by a doctor
    }

    public enum PainLevel {
        NONE(0, "No pain"),
        MILD(1, "Mild pain"),
        MODERATE(2, "Moderate pain"),
        SEVERE(3, "Severe pain"),
        VERY_SEVERE(4, "Very severe pain"),
        WORST_POSSIBLE(5, "Worst possible pain");

        private final int level;
        private final String description;

        PainLevel(int level, String description) {
            this.level = level;
            this.description = description;
        }

        public int getLevel() { return level; }
        public String getDescription() { return description; }
    }

    public enum Component {
        BODY_TEMPERATURE,
        PULSE_RATE,
        RESPIRATORY_RATE,
        SYSTOLIC_BP,
        DIASTOLIC_BP,
        OXYGEN_SATURATION
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id", referencedColumnName = "user_id", nullable = false)
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "user_id", nullable = false)
    private Patient patient;

    private LocalDateTime timestamp;

    private double bodyTemperature;
    private int pulseRate;
    private int respiratoryRate;

    @Embedded
    private BloodPressure bloodPressure;

    private double oxygenSaturation;
    private Double height; // cm
    private Double weight; // kg

    private PainLevel painLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.REQUESTED;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "feedback_id")
    private Feedback feedback;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "prescription_id")
    private Prescription prescription;

    // Constructors
    public VitalSigns() {}

    public VitalSigns(LocalDateTime timestamp, double bodyTemperature, int pulseRate, int respiratoryRate,
                     BloodPressure bloodPressure, double oxygenSaturation, Double height, Double weight,
                     Patient patient, Doctor doctor) {
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        this.bodyTemperature = bodyTemperature;
        this.pulseRate = pulseRate;
        this.respiratoryRate = respiratoryRate;
        this.bloodPressure = Objects.requireNonNull(bloodPressure, "Blood pressure cannot be null");
        this.oxygenSaturation = oxygenSaturation;
        this.painLevel = PainLevel.NONE;
        this.height = height;
        this.weight = weight;
        this.patient = Objects.requireNonNull(patient, "Patient cannot be null");
        this.doctor = Objects.requireNonNull(doctor, "Doctor cannot be null");
        this.feedback = null;
        this.prescription = null;
    }

    // Getters
    public Long getId() { return id; }
    public Doctor getDoctor() { return doctor; }
    public Patient getPatient() { return patient; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getBodyTemperature() { return bodyTemperature; }
    public int getPulseRate() { return pulseRate; }
    public int getRespiratoryRate() { return respiratoryRate; }
    public BloodPressure getBloodPressure() { return bloodPressure; }
    public double getOxygenSaturation() { return oxygenSaturation; }
    public Double getHeight() { return height; }
    public Double getWeight() { return weight; }
    public PainLevel getPainLevel() { return painLevel; }
    public Status getStatus() { return status; }
    public Feedback getFeedback() { return feedback; }
    public Prescription getPrescription() { return prescription; }

    // Setters
    public void setDoctor(Doctor doctor) {
        this.doctor = Objects.requireNonNull(doctor, "Doctor cannot be null");
    }

    public void setPatient(Patient patient) {
        this.patient = Objects.requireNonNull(patient, "Patient cannot be null");
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        if (timestamp.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Timestamp cannot be in the future");
        }
    }

    public void setBodyTemperature(double bodyTemperature) {
        if (bodyTemperature < 25 || bodyTemperature > 45) {
            throw new IllegalArgumentException("Body temperature must be between 25°C and 45°C");
        }
        this.bodyTemperature = bodyTemperature;
    }

    public void setPulseRate(int pulseRate) {
        if (pulseRate < 30 || pulseRate > 200) {
            throw new IllegalArgumentException("Pulse rate must be between 30 and 200 bpm");
        }
        this.pulseRate = pulseRate;
    }

    public void setRespiratoryRate(int respiratoryRate) {
        if (respiratoryRate < 5 || respiratoryRate > 60) {
            throw new IllegalArgumentException("Respiratory rate must be between 5 and 60 breaths/min");
        }
        this.respiratoryRate = respiratoryRate;
    }

    public void setBloodPressure(BloodPressure bloodPressure) {
        this.bloodPressure = Objects.requireNonNull(bloodPressure, "Blood pressure cannot be null");
    }

    public void setOxygenSaturation(double oxygenSaturation) {
        if (oxygenSaturation < 50 || oxygenSaturation > 100) {
            throw new IllegalArgumentException("Oxygen saturation must be between 50% and 100%");
        }
        this.oxygenSaturation = oxygenSaturation;
    }

    public void setHeight(Double height) {
        if (height != null && (height < 30 || height > 250)) {
            throw new IllegalArgumentException("Height must be between 30cm and 250cm");
        }
        this.height = height;
    }

    public void setWeight(Double weight) {
        if (weight != null && (weight < 1 || weight > 300)) {
            throw new IllegalArgumentException("Weight must be between 1kg and 300kg");
        }
        this.weight = weight;
    }

    public void setPainLevel(PainLevel painLevel) {
        this.painLevel = painLevel;
    }

    public void setStatus(Status status) {
        this.status = Objects.requireNonNull(status, "Status cannot be null");
    }

    public void setFeedback(Feedback feedback) {
        this.feedback = feedback;
    }

    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;
    }

    public double getComponent(Component component) {
        return switch (component) {
            case BODY_TEMPERATURE -> getBodyTemperature();
            case PULSE_RATE -> getPulseRate();
            case RESPIRATORY_RATE -> getRespiratoryRate();
            case SYSTOLIC_BP -> getBloodPressure().getSystolic();
            case DIASTOLIC_BP -> getBloodPressure().getDiastolic();
            case OXYGEN_SATURATION -> getOxygenSaturation();
        };
    }

    // Inner BloodPressure class
    @Embeddable
    public static class BloodPressure {
        private int systolic;
        private int diastolic;

        @Enumerated(EnumType.STRING)
        private Category category;

        public BloodPressure() {}

        public BloodPressure(int systolic, int diastolic) {
            if (systolic <= 0 || diastolic <= 0 || systolic < diastolic) {
                throw new IllegalArgumentException("Invalid blood pressure values");
            }
            this.systolic = systolic;
            this.diastolic = diastolic;
            this.category = determineCategory(systolic, diastolic);
        }

        private Category determineCategory(int systolic, int diastolic) {
            if (systolic >= 180 || diastolic >= 120) return Category.HYPERTENSIVE_CRISIS;
            if (systolic >= 140 || diastolic >= 90) return Category.STAGE_2_HYPERTENSION;
            if (systolic >= 130 || diastolic >= 80) return Category.STAGE_1_HYPERTENSION;
            if (systolic >= 120) return Category.ELEVATED;
            return Category.NORMAL;
        }

        public boolean isCritical() {
            return category == Category.HYPERTENSIVE_CRISIS || systolic < 90 || diastolic < 60;
        }

        public String getBloodPressureReading() {
            return systolic + "/" + diastolic + " mmHg";
        }

        public int getSystolic() { return systolic; }
        public int getDiastolic() { return diastolic; }
        public Category getCategory() { return category; }

        public enum Category {
            NORMAL("Normal"),
            ELEVATED("Elevated"),
            STAGE_1_HYPERTENSION("Stage 1 Hypertension"),
            STAGE_2_HYPERTENSION("Stage 2 Hypertension"),
            HYPERTENSIVE_CRISIS("Hypertensive Crisis");

            private final String description;

            Category(String description) {
                this.description = description;
            }

            public String getDescription() {
                return description;
            }
        }
    }
}