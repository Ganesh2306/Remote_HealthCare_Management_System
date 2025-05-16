package com.example.demo.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_tokens")
public class VerificationToken {

    public enum EmailType {
        VERIFICATION,
        ACCEPTANCE,
        REJECTION
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    private LocalDateTime expiryDate;

    @Enumerated(EnumType.STRING)
    private EmailType emailType;

    public VerificationToken() {
        this.token = UUID.randomUUID().toString();
        this.expiryDate = LocalDateTime.now().plusHours(24);
        this.emailType = EmailType.VERIFICATION; // Default to verification
    }

    public VerificationToken(User user) {
        this();
        this.user = user;
    }

    public VerificationToken( User user, EmailType emailType) {
        this.token = UUID.randomUUID().toString();
        this.user = user;
        this.expiryDate = LocalDateTime.now().plusHours(24);
        this.emailType = emailType;
    }

    // Getters and setters
    public Long getId() { return id; }
    public String getToken() { return token; }
    public User getUser() { return user; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public EmailType getEmailType() { return emailType; }

    public void setUser(User user) { this.user = user; }
    public void setEmailType(EmailType emailType) { this.emailType = emailType; }
    public boolean isExpired() { return LocalDateTime.now().isAfter(expiryDate); }

  
}