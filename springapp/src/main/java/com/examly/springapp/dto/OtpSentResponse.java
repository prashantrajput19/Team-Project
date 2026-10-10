package com.examly.springapp.dto;

public record OtpSentResponse(long cooldownSeconds, long expiresInSeconds, String devCode) {
}