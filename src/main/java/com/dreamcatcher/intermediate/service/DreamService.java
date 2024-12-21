package com.dreamcatcher.intermediate.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dreamcatcher.intermediate.model.Dream;
import com.dreamcatcher.intermediate.model.User;
import com.dreamcatcher.intermediate.repository.DreamRepository;

@Service
public class DreamService {

    @Autowired
    private DreamRepository dreamRepository;

    public Dream createDream(String content, User user) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Dream content cannot be null or blank");
        }

        Dream dream = new Dream();
        dream.setContent(content);
        dream.setCreatedAt(LocalDateTime.now());
        dream.setUser(user);

        return dreamRepository.save(dream);
    }

    public List<Dream> getRecentDreams() {
        return dreamRepository.findAll();
    }

    public List<Dream> searchDreams(String query) {
        return dreamRepository.findByContentContainingIgnoreCase(query);
    }

    public Optional<Dream> getDreamById(Long id) {
        return dreamRepository.findById(id);
    }

    public void deleteDream(Long id, User user) {
        Optional<Dream> dream = dreamRepository.findById(id);
        if (dream.isEmpty() || !dream.get().getUser().equals(user)) {
            throw new IllegalArgumentException("Dream not found or unauthorized access");
        }
        dreamRepository.deleteById(id);
    }
}
