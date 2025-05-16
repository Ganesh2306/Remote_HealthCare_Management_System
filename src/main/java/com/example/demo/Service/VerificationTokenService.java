package com.example.demo.Service;

import com.example.demo.Models.VerificationToken;
import com.example.demo.respository.VerificationTokenRepository;
import com.example.demo.Exceptions.InvalidTokenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Service
@Transactional
public class  VerificationTokenService {

    private final VerificationTokenRepository tokenRepository;
   
    
    @Value("${app.verification.token.expiry-hours}")
    private int expiryHours;

    @Autowired
    public VerificationTokenService(VerificationTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public VerificationToken validateToken(String token) throws InvalidTokenException {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
            .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Verification token has expired");
        }

        if (verificationToken.getUser().isEnabled()) {
            throw new InvalidTokenException("Account is already verified");
        }

        return verificationToken;
    }


 
    public Optional<VerificationToken> findToken(String token) {
        return tokenRepository.findByToken(token);
    }

  
    public void deleteToken(VerificationToken token) {
        tokenRepository.delete(token);
    }
    public List<VerificationToken> findExpiredTokens(LocalDateTime expiryDate) {
        return tokenRepository.findByExpiryDateBefore(expiryDate);
    }
}