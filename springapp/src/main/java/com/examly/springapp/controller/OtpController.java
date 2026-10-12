package com.examly.springapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.examly.springapp.dto.OtpSendRequest;
import com.examly.springapp.dto.OtpSentResponse;
import com.examly.springapp.dto.OtpVerifyRequest;
import com.examly.springapp.service.OtpService;

@RestController
@RequestMapping("/api/otp")
public class OtpController {
    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/send")
    public OtpSentResponse send(@RequestBody OtpSendRequest request) {
        return otpService.sendOtp(request.email());
    }

    @PostMapping("/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verify(@RequestBody OtpVerifyRequest request) {
        otpService.verifyOtp(request.email(), request.code());
    }
}