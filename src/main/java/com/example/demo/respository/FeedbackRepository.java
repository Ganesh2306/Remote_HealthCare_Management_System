package com.example.demo.respository;



import com.example.demo.Models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByPatientAndDoctorOrderByCreatedAtDesc(Patient patient, Doctor doctor);
}