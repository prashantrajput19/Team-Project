package com.examly.springapp.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.examly.springapp.config.SmtpMailer;
import com.examly.springapp.dto.OtpSentResponse;
import com.examly.springapp.exceptions.InvalidRequestException;
import com.examly.springapp.model.Otp;
import com.examly.springapp.repository.OtpRepo;
import com.examly.springapp.repository.UserRepo;

@Service
public class OtpService {
    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private final OtpRepo otpRepo;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final SmtpMailer mailer;
    private final long expiryMinutes;
    private final long cooldownSeconds;
    private final int maxAttempts;
    private final boolean showUndeliveredCode;

    public OtpService(OtpRepo otpRepo, UserRepo userRepo, PasswordEncoder passwordEncoder, SmtpMailer mailer,
                      @Value("${app.otp.expiry-minutes:5}") long expiryMinutes,
                      @Value("${app.otp.resend-cooldown-seconds:60}") long cooldownSeconds,
                      @Value("${app.otp.max-attempts:5}") int maxAttempts,
                      @Value("${app.otp.show-undelivered-code:false}") boolean showUndeliveredCode) {
        this.otpRepo = otpRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.mailer = mailer;
        this.expiryMinutes = expiryMinutes;
        this.cooldownSeconds = cooldownSeconds;
        this.maxAttempts = maxAttempts;
        this.showUndeliveredCode = showUndeliveredCode;
    }

    public OtpSentResponse sendOtp(String rawEmail) {
        String email = normalize(rawEmail);
        if (!email.matches(EMAIL_PATTERN)) {
            throw new InvalidRequestException("Please enter a valid email");
        }
        if (userRepo.findByEmail(email).isPresent()) {
            throw new InvalidRequestException("This email is already registered");
        }

        Otp otp = otpRepo.findByEmail(email).orElseGet(() -> new Otp(email));
        if (otp.getSentAt() != null) {
            long waited = Duration.between(otp.getSentAt(), LocalDateTime.now()).getSeconds();
            if (waited < cooldownSeconds) {
                throw new InvalidRequestException("Please wait " + (cooldownSeconds - waited) + " seconds before requesting a new OTP");
            }
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        boolean delivered = deliver(email, code);

        LocalDateTime now = LocalDateTime.now();
        otp.reset(passwordEncoder.encode(code), now, now.plusMinutes(expiryMinutes));
        otpRepo.save(otp);

        return new OtpSentResponse(cooldownSeconds, expiryMinutes * 60,
                !delivered && showUndeliveredCode ? code : null);
    }

    public void verifyOtp(String rawEmail, String code) {
        Otp otp = otpRepo.findByEmail(normalize(rawEmail))
                .orElseThrow(() -> new InvalidRequestException("No OTP was requested for this email"));

        if (otp.isVerified()) {
            return;
        }
        if (LocalDateTime.now().isAfter(otp.getExpiresAt())) {
            throw new InvalidRequestException("OTP has expired. Please request a new one");
        }
        if (otp.getAttempts() >= maxAttempts) {
            throw new InvalidRequestException("Too many wrong attempts. Please request a new OTP");
        }
        if (code == null || !passwordEncoder.matches(code.trim(), otp.getCodeHash())) {
            otp.recordFailedAttempt();
            otpRepo.save(otp);
            throw new InvalidRequestException("Invalid OTP");
        }

        otp.markVerified();
        otpRepo.save(otp);
    }

    private boolean deliver(String email, String code) {
        if (!mailer.isConfigured()) {
            if (showUndeliveredCode) {
                log.warn("[DEV] No mail server configured (MAIL_HOST). OTP for {} is {}", email, code);
                return false;
            }
            log.error("Cannot send OTP: no mail server configured (set MAIL_HOST, MAIL_USERNAME, MAIL_PASSWORD)");
            throw new InvalidRequestException("Could not send the OTP right now. Please try again later");
        }
        try {
            mailer.send(email, "Your ArchitectPoint verification code",
                    "Your ArchitectPoint verification code is " + code + ". It expires in " + expiryMinutes
                            + " minutes. If you did not request it, ignore this email.");
            return true;
        } catch (Exception exception) {
            log.error("Could not email the OTP to {}: {}", email, exception.getMessage());
            throw new InvalidRequestException("Could not send the OTP right now. Please try again later");
        }
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}