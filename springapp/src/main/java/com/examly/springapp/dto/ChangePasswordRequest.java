package com.examly.springapp.dto;

public record ChangePasswordRequest(String currentPassword, String newPassword) {
}