package com.dreamcatcher.intermediate.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dreamcatcher.intermediate.model.Dream;
import com.dreamcatcher.intermediate.model.User;
import com.dreamcatcher.intermediate.service.DreamService;

@RestController
@RequestMapping("/dreams")
public class DreamController {

    @Autowired
    private DreamService dreamService;

    @PostMapping("/create")
    public ResponseEntity<?> createDream(@RequestBody String content, Principal principal) {
        try {
            // Assuming User is fetched based on the Principal (username)
            User user = getUserFromPrincipal(principal);
            Dream dream = dreamService.createDream(content, user);
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
    

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDream(@PathVariable Long id, Principal principal) {
        try {
            User user = getUserFromPrincipal(principal);
            dreamService.deleteDream(id, user);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private User getUserFromPrincipal(Principal principal) {
        User user = new User();
        user.setUsername(principal.getName());
        return user;
    }
}
