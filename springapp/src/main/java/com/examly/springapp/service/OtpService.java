package com.examly.springapp.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.examly.springapp.dto.OtpSentResponse;
import com.examly.springapp.exceptions.InvalidRequestException;
import com.examly.springapp.model.Otp;
import com.examly.springapp.repository.OtpRepo;
import com.examly.springapp.repository.UserRepo;

// Sends a 6-digit code to an email address and checks the code the user types in
@Service
public class OtpService {
    private static final int EXPIRY_MINUTES = 5;    // the code works for 5 minutes
    private static final int COOLDOWN_SECONDS = 60; // wait 60 seconds before asking for a new code
    private static final int MAX_ATTEMPTS = 5;      // 5 wrong tries, then a new code is needed

    private final OtpRepo otpRepo;

    private final UserRepo userRepo;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    public OtpService(
        OtpRepo otpRepo,
        UserRepo userRepo,
        PasswordEncoder passwordEncoder,
        EmailService emailService) {
        this.otpRepo = otpRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public OtpSentResponse sendOtp(String email) {
        if (userRepo.findByEmail(email).isPresent()) {
            throw new InvalidRequestException("This email is already registered");
        }

        Otp otp = otpRepo.findByEmail(email).orElse(new Otp(email));
        if (otp.getSentAt() != null) {
            long waited = Duration.between(otp.getSentAt(), LocalDateTime.now()).getSeconds();
            if (waited < COOLDOWN_SECONDS) {
                throw new InvalidRequestException("Please wait " + (COOLDOWN_SECONDS - waited) + " seconds before requesting a new OTP");
            }
        }

        // Random number from 000000 to 999999
        String code = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        sendOtpEmail(email, code);

        // Save the code encrypted, like a password
        LocalDateTime now = LocalDateTime.now();
        otp.reset(passwordEncoder.encode(code), now, now.plusMinutes(EXPIRY_MINUTES));
        otpRepo.save(otp);

        return new OtpSentResponse(COOLDOWN_SECONDS);
    }

    public void verifyOtp(String email, String code) {
        Otp otp = otpRepo.findByEmail(email)
                .orElseThrow(() -> new InvalidRequestException("No OTP was requested for this email"));

        if (otp.isVerified()) {
            return;
        }
        if (LocalDateTime.now().isAfter(otp.getExpiresAt())) {
            throw new InvalidRequestException("OTP has expired. Please request a new one");
        }
        if (otp.getAttempts() >= MAX_ATTEMPTS) {
            throw new InvalidRequestException("Too many wrong attempts. Please request a new OTP");
        }
        if (!passwordEncoder.matches(code, otp.getCodeHash())) {
            otp.recordFailedAttempt();
            otpRepo.save(otp);
            throw new InvalidRequestException("Invalid OTP");
        }

        otp.markVerified();
        otpRepo.save(otp);
    }

    private void sendOtpEmail(String email, String code) {
        String subject = "Your ArchitectPoint verification code";
        String body = "Your verification code is " + code + ".\n"
                + "It expires in " + EXPIRY_MINUTES + " minutes.\n"
                + "If you did not request it, please ignore this email.";
        try {
            emailService.sendEmail(email, subject, body);
        } catch (Exception e) {
            System.out.println("Could not send OTP email: " + e.getMessage());
            throw new InvalidRequestException("Could not send the OTP right now. Please try again later");
        }
    }
}
