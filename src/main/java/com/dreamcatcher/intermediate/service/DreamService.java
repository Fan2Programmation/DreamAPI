package com.dreamcatcher.intermediate.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dreamcatcher.intermediate.model.Dream;
import com.dreamcatcher.intermediate.model.User;
import com.dreamcatcher.intermediate.repository.DreamRepository;
import com.dreamcatcher.intermediate.repository.UserRepository;

@Service
public class DreamService {

    @Autowired
    private DreamRepository dreamRepository;

    @Autowired
    private UserRepository userRepository;

    public Dream saveDream(String content, String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found with username: " + username);
        }

        User user = userOptional.get();

        Dream dream = new Dream();
        dream.setContent(content);
        dream.setUser(user);
        dream.setCreatedAt(LocalDateTime.now());

        return dreamRepository.save(dream);
    }

    public List<Dream> getAllDreams() {
        return dreamRepository.findAll();
    }

    public List<Dream> searchDreams(String query) {
        return dreamRepository.findByContentContainingIgnoreCase(query);
    }

    public Optional<Dream> getDreamById(Long id) {
        return dreamRepository.findById(id);
    }

    public boolean deleteDream(Long id, String username) {
        Optional<Dream> dreamOptional = dreamRepository.findById(id);

        if (dreamOptional.isPresent()) {
            Dream dream = dreamOptional.get();

            if (dream.getUser().getUsername().equals(username)) {
                dreamRepository.delete(dream);
                return true;
            } else {
                throw new IllegalArgumentException("Unauthorized access to delete this dream.");
            }
        } else {
            return false;
        }
    }
}
