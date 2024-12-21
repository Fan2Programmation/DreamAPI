package com.dreamcatcher.intermediate.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dreamcatcher.intermediate.model.User;
import com.dreamcatcher.intermediate.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(String username, String rawPassword) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(rawPassword);
        newUser.setCreationDate(java.time.LocalDateTime.now());

        return userRepository.save(newUser);
    }

    public boolean authenticateUser(String username, String rawPassword) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            return false;
        }

        return rawPassword.equals(user.get().getPassword());
    }
}
