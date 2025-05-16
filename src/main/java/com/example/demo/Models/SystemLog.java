package com.example.demo.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "system_logs", indexes = {
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
public class SystemLog {

    public enum Severity {
        INFO, WARNING, ERROR, CRITICAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", referencedColumnName = "user_id", nullable = false)
    private Admin admin;

    @Column(nullable = false, length = 1000)
    private String action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    public SystemLog() {
        // Default constructor
    }

    public SystemLog(Admin admin, String action, Severity severity) {
        this.admin = Objects.requireNonNull(admin, "Admin cannot be null");
        this.action = Objects.requireNonNull(action, "Action cannot be null");
        this.severity = Objects.requireNonNull(severity, "Severity cannot be null");
    }

    @PrePersist
    public void prePersist() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }
}
