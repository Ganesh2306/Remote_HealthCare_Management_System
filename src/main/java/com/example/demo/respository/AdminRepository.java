package com.example.demo.respository;

import java.util.Optional;

import com.example.demo.Models.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface  AdminRepository extends JpaRepository<Admin, String> {
      // Custom query to find admin by user email
    Optional<Admin> findByEmail(String email);
    
    List<Admin> findAllByOrderByUserIdAsc();
}
