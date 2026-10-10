package com.examly.springapp.dto;

import com.examly.springapp.model.User;

public record UserResponse(Long userId, String email, String username, String mobileNumber,
                           String userRole) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getUserId(), user.getEmail(), user.getUsername(),
                user.getMobileNumber(), user.getUserRole());
    }
}