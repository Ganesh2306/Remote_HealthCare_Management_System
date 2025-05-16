package com.example.demo.respository;


import com.example.demo.Models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByPatientAndPrescribingDoctorOrderByDatePrescribedDesc(Patient patient, Doctor doctor);
    boolean existsByPatientAndPrescribingDoctor(Patient patient, Doctor doctor);
}