package com.examly.springapp.service;

import java.util.List;

import com.examly.springapp.model.User;

public interface UserService {

    User createUser(User user);

    User loginUser(User user);

    User getUserByEmail(String email);

    void changePassword(String email, String currentPassword, String newPassword);
}
