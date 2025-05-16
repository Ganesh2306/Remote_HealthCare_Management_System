package com.example.demo.respository;



import com.example.demo.Models.User;
import com.example.demo.Models.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
     // Delete patient by email
    @Transactional
    void deleteByEmail(String email);
    void deleteById(String userId);

     
    @Query("SELECT a FROM Admin a JOIN a.managedDoctors d WHERE d.userId = :doctorId")
    Optional<Admin> findAdminByManagedDoctor(String doctorId);
    
    @Query("SELECT a FROM Admin a JOIN a.managedPatients p WHERE p.userId = :patientId")
    Optional<Admin> findAdminByManagedPatient(String patientId);


    Optional<User> findByUserId(String userId);

}