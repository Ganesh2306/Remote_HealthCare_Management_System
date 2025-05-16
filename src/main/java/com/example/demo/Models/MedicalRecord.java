package com.example.demo.Models;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "user_id", nullable = false, unique = true)
    private Patient patient;

    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Immunization> immunizations= new ArrayList<>();

    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LabResult> labResults=new ArrayList<>();

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

    public List<Immunization> getImmunizations() {
        return immunizations;
    }

    public void setImmunizations(List<Immunization> immunizations) {
        this.immunizations = immunizations;
    }

    public List<LabResult> getLabResults() {
        return labResults;
    }

    public void setLabResults(List<LabResult> labResults) {
        this.labResults = labResults;
    }

    // Inner Immunization class
    @Entity
    public static class Immunization {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        
        private String vaccineName;
        private java.time.LocalDate administrationDate;
        
        @ManyToOne
        @JoinColumn(name = "medical_record_id")
        private MedicalRecord medicalRecord;

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getVaccineName() {
            return vaccineName;
        }

        public void setVaccineName(String vaccineName) {
            this.vaccineName = vaccineName;
        }

        public java.time.LocalDate getAdministrationDate() {
            return administrationDate;
        }

        public void setAdministrationDate(java.time.LocalDate administrationDate) {
            this.administrationDate = administrationDate;
        }

        public MedicalRecord getMedicalRecord() {
            return medicalRecord;
        }

        public void setMedicalRecord(MedicalRecord medicalRecord) {
            this.medicalRecord = medicalRecord;
        }
    }

    // Inner LabResult class
    @Entity
    public static class LabResult {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        
        private String testName;
        private java.time.LocalDate testDate;
        private String result;
     
        
        @ManyToOne
        @JoinColumn(name = "medical_record_id")
        private MedicalRecord medicalRecord;

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTestName() {
            return testName;
        }

        public void setTestName(String testName) {
            this.testName = testName;
        }

        public java.time.LocalDate getTestDate() {
            return testDate;
        }

        public void setTestDate(java.time.LocalDate testDate) {
            this.testDate = testDate;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }


        public MedicalRecord getMedicalRecord() {
            return medicalRecord;
        }

        public void setMedicalRecord(MedicalRecord medicalRecord) {
            this.medicalRecord = medicalRecord;
        }
    }
}