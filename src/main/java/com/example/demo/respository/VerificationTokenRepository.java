package com.example.demo.respository;


import com.example.demo.Models.User;
import com.example.demo.Models.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    
    @Modifying
    @Query("DELETE FROM VerificationToken t WHERE t.user = :user")
    void deleteByUser(User user);

    
    List<VerificationToken> findByExpiryDateBefore(LocalDateTime date);
}