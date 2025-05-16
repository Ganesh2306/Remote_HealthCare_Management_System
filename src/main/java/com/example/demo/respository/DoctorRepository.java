package com.example.demo.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.Models.Doctor;


import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    
    // Get doctor by userId (assuming userId is unique and present in the Doctor entity)
    Optional<Doctor> findByUserId(String userId);
    Optional<Doctor> findByEmail(String email);
    Page<Doctor> findAll(Pageable pageable);
    boolean existsByEmail(String email);
    List<Doctor> findByIsActiveTrue();
Page<Doctor> findByIsActiveTrue(Pageable pageable);

    boolean existsByUserId(String userId);
     Page<Doctor> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName, Pageable pageable);
}

