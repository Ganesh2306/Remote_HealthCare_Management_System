package com.example.demo.Service;



import com.example.demo.Models.*;
import com.example.demo.respository.PrescriptionRepository;
import com.example.demo.respository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;


    @Autowired
    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                              PatientRepository patientRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.patientRepository = patientRepository;

    }

    public Patient getPatientById(String patientId) {
        return patientRepository.findByUserId(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    public List<Prescription> getPatientPrescriptions(Patient patient, Doctor doctor) {
        return prescriptionRepository.findByPatientAndPrescribingDoctorOrderByDatePrescribedDesc(
                patient, doctor);
    }

    @Transactional
    public Prescription createPrescription( String patientId, Doctor doctor,
                                         String medication, Prescription.Dosage dosage,
                                         LocalDate startDate, LocalDate endDate, 
                                         String instructions) {
        Patient patient = getPatientById(patientId);


        Prescription prescription = new Prescription();
        prescription.setPrescribingDoctor(doctor);
        prescription.setPatient(patient);
        prescription.setMedication(medication);
        prescription.setDosage(dosage);
        prescription.setDatePrescribed(LocalDate.now());
        prescription.setStartDate(startDate);
        prescription.setEndDate(endDate);
        prescription.setInstructions(instructions != null ? instructions : "Take as directed");
        prescription.setStatus(Prescription.PrescriptionStatus.ACTIVE);
        return prescriptionRepository.save(prescription);
    }
}