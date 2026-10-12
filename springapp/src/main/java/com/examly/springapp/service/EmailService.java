package com.examly.springapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// Sends simple text emails.
// Spring creates the JavaMailSender for us using the spring.mail.* settings
// in application.properties (host, port, username, password).
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    // The email address the mail is sent from
    private final String fromEmail;

    public EmailService(
        JavaMailSender mailSender,
        @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}
