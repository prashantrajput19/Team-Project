package com.examly.springapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.examly.springapp.exceptions.InvalidCredentialsException;
import com.examly.springapp.exceptions.InvalidRequestException;
import com.examly.springapp.exceptions.ResourceNotFoundException;
import com.examly.springapp.model.User;
import com.examly.springapp.repository.UserRepo;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User createUser(User user) {
        user.setUserId(null);
        user.setUserRole("User");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    @Override
    public User loginUser(User user) {
        return userRepo.findByEmail(user.getEmail())
                .filter(found -> passwordEncoder.matches(user.getPassword(), found.getPassword()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    @Override
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = getUserByEmail(email);
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new InvalidRequestException("New password must be at least 6 characters.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }
}
