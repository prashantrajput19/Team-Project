package com.examly.springapp.dto;

public record OtpVerifyRequest(String email, String code) {
}