package com.example.demo.respository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.Models.MedicalRecord;

import java.util.Optional;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    Optional<MedicalRecord> findByPatientUserId(String patientId);
}