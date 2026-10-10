package com.examly.springapp.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Otp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long otpId;

    @Column(unique = true, nullable = false)
    private String email;

    private String codeHash;

    private LocalDateTime sentAt;

    private LocalDateTime expiresAt;

    private int attempts;

    private boolean verified;

    public Otp() {
    }

    public Otp(String email) {
        this.email = email;
    }

    public void reset(String codeHash, LocalDateTime sentAt, LocalDateTime expiresAt) {
        this.codeHash = codeHash;
        this.sentAt = sentAt;
        this.expiresAt = expiresAt;
        this.attempts = 0;
        this.verified = false;
    }

    public Long getOtpId() {
        return otpId;
    }

    public String getEmail() {
        return email;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public void recordFailedAttempt() {
        attempts++;
    }

    public boolean isVerified() {
        return verified;
    }

    public void markVerified() {
        verified = true;
    }
}