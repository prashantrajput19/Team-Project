package com.examly.springapp.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.examly.springapp.config.JwtUtils;
import com.examly.springapp.dto.ChangePasswordRequest;
import com.examly.springapp.dto.LoginDTO;
import com.examly.springapp.dto.UserResponse;
import com.examly.springapp.model.User;
import com.examly.springapp.service.UserService;

@RestController
@RequestMapping("/api")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public User register(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PostMapping("/login")
    public LoginDTO login(@RequestBody User loginRequest) {
        User user = userService.loginUser(loginRequest);

        LoginDTO dto = new LoginDTO();
        dto.setToken(jwtUtils.generateToken(user.getEmail()));
        dto.setUsername(user.getUsername());
        dto.setUserRole(user.getUserRole());
        dto.setUserId(user.getUserId());

        return dto;
    }

    @GetMapping("/me")
    public UserResponse me(Principal principal) {
        return UserResponse.from(userService.getUserByEmail(principal.getName()));
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestBody ChangePasswordRequest request, Principal principal) {
        userService.changePassword(principal.getName(), request.currentPassword(), request.newPassword());
    }
}