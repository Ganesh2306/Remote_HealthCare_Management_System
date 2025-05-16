package com.example.demo.respository;

import com.example.demo.Models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, String> {

    // Custom query to find patient by user email
    Optional<Patient> findByEmail(String email);
    
    // Check if patient exists by email
    boolean existsByEmail(String email);
    
    // Delete patient by email
    @Transactional
    void deleteByEmail(String email);

    Optional<Patient> findByUserId(String UserId);


    Page<Patient> findAll(Pageable pageable);
    Page<Patient> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName, Pageable pageable);
}