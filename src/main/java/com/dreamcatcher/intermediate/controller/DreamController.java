package com.dreamcatcher.intermediate.controller;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

import com.dreamcatcher.intermediate.dto.DreamCreationRequest;
import com.dreamcatcher.intermediate.model.Dream;
import com.dreamcatcher.intermediate.service.DreamService;

@RestController
@RequestMapping("/dreams")
public class DreamController {

    @Autowired
    private DreamService ds;

    @PostMapping("/create")
    public ResponseEntity<?> createDream(@RequestBody DreamCreationRequest req) {
        try {
            Dream newD = ds.saveDream(req.getContent(), req.getUsername());
            return ResponseEntity.ok(newD.getId());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Map<String, Object>>> getRecent() {
        List<Map<String, Object>> all = ds.getAllDreams().stream().map(d -> {
            Map<String, Object> bag = new HashMap<>();
            bag.put("content", d.getContent());
            bag.put("author", d.getUser().getUsername());
            bag.put("date", d.getCreatedAt().toString());
            if (d.getImageData() != null && d.getImageData().length > 0) {
                bag.put("imageData", Base64.getEncoder().encodeToString(d.getImageData()));
            } else {
                bag.put("imageData", "");
            }
            return bag;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(all);
    }    

    @GetMapping("/search")
    public List<Map<String, Object>> searchDreams(@RequestParam String query) {
        List<Dream> hits = ds.searchDreams(query);
        List<Map<String, Object>> res = new ArrayList<>();
        for (Dream d : hits) {
            Map<String, Object> item = new HashMap<>();
            item.put("content", d.getContent());
            item.put("user", d.getUser().getUsername());
            item.put("createdAt", d.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            if (d.getImageData() != null && d.getImageData().length > 0) {
                item.put("imageData", Base64.getEncoder().encodeToString(d.getImageData()));
            } else {
                item.put("imageData", "");
            }
            res.add(item);
        }
        return res;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        Optional<Dream> found = ds.getDreamById(id);
        if (found.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No dream");
        }
        Dream e = found.get();
        Map<String, Object> r = new HashMap<>();
        r.put("content", e.getContent());
        r.put("author", e.getUser().getUsername());
        r.put("date", e.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        if (e.getImageData() != null && e.getImageData().length > 0) {
            r.put("imageData", Base64.getEncoder().encodeToString(e.getImageData()));
        } else {
            r.put("imageData", "");
        }
        return ResponseEntity.ok(r);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @RequestParam String username) {
        try {
            boolean removed = ds.deleteDream(id, username);
            if (removed) return ResponseEntity.ok("Deleted");
            else return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}
