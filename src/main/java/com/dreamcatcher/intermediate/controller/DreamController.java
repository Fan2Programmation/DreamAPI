package com.dreamcatcher.intermediate.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dreamcatcher.intermediate.model.Dream;
import com.dreamcatcher.intermediate.service.DreamService;

@RestController
@RequestMapping("/dreams")
public class DreamController {

    @Autowired
    private DreamService dreamService;

    @PostMapping("/create")
    public ResponseEntity<?> createDream(@RequestParam String content, @RequestParam String username) {
        try {
            Dream dream = dreamService.saveDream(content, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(dream);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Dream>> getRecentDreams() {
        List<Dream> dreams = dreamService.getRecentDreams();
        return ResponseEntity.ok(dreams);
    }

    @GetMapping("/search")
    public List<Dream> searchDreams(@RequestParam String query) {
        return dreamService.searchDreams(query);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDreamById(@PathVariable Long id) {
        Optional<Dream> dream = dreamService.getDreamById(id);
        if (dream.isPresent()) {
            return ResponseEntity.ok(dream.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dream not found");
        }
    }
    
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDream(@PathVariable Long id, @RequestParam String username) {
        try {
            boolean deleted = dreamService.deleteDream(id, username);
            if (deleted) {
                return ResponseEntity.ok("Dream deleted successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dream not found.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}

