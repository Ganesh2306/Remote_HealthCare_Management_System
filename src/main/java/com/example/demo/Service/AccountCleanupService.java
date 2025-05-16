package com.example.demo.Service;

import com.example.demo.Models.*;


import com.example.demo.respository.PatientRepository;
import com.example.demo.respository.VerificationTokenRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.respository.UserRepository;
import com.example.demo.Exceptions.*;

@Service
@Transactional
public class AccountCleanupService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    public AccountCleanupService(UserRepository userRepository,
                               PatientRepository patientRepository,
                               VerificationTokenRepository tokenRepository,
                               EmailService emailService) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    public void handleFailedVerification(String tokenValue) {
        VerificationToken token = tokenRepository.findByToken(tokenValue)
            .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        User user = token.getUser();
        
        // Send rejection email
        emailService.sendRejectionEmail(user.getEmail(), user.getFirstName());
        
        // Delete patient if the user is a patient
        if (user instanceof Patient) {
            patientRepository.delete((Patient) user);
        }
        
        // This will delete the user due to the inheritance
        userRepository.delete(user);
        
        // Clean up the token
        tokenRepository.delete(token);
    }
}