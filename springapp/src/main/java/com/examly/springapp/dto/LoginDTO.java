package com.examly.springapp.dto;

public record LoginDTO(String token, String username, String userRole, Long userId) {
}
