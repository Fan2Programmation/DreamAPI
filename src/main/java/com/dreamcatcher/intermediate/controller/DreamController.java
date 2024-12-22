package com.dreamcatcher.intermediate.controller;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.dreamcatcher.intermediate.dto.DreamCreationRequest;
import com.dreamcatcher.intermediate.model.Dream;
import com.dreamcatcher.intermediate.service.DreamService;
import org.springframework.util.Base64Utils;

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
        List<Dream> list = ds.getAllDreams();
        List<Map<String, Object>> mapped = list.stream().map(d -> {
            Map<String, Object> m = new HashMap<>();
            m.put("content", d.getContent());
            m.put("author", d.getUser().getUsername());
            m.put("date", d.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            if (d.getImageData() != null && d.getImageData().length > 0) {
                m.put("imageData", Base64Utils.encodeToString(d.getImageData()));
            } else {
                m.put("imageData", "");
            }
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(mapped);
    }

    @GetMapping("/search")
    public List<Map<String, Object>> searchDreams(@RequestParam String query) {
        List<Dream> hits = ds.searchDreams(query);
        List<Map<String, Object>> mapped = new ArrayList<>();
        for (Dream d : hits) {
            Map<String, Object> o = new HashMap<>();
            o.put("content", d.getContent());
            o.put("author", d.getUser().getUsername());
            o.put("date", d.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            if (d.getImageData() != null && d.getImageData().length > 0) {
                o.put("imageData", Base64Utils.encodeToString(d.getImageData()));
            } else {
                o.put("imageData", "");
            }
            mapped.add(o);
        }
        return mapped;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        Optional<Dream> found = ds.getDreamById(id);
        if (found.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No dream");
        }
        Dream d = found.get();
        Map<String, Object> r = new HashMap<>();
        r.put("content", d.getContent());
        r.put("author", d.getUser().getUsername());
        r.put("date", d.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        if (d.getImageData() != null && d.getImageData().length > 0) {
            r.put("imageData", Base64Utils.encodeToString(d.getImageData()));
        } else {
            r.put("imageData", "");
        }
        return ResponseEntity.ok(r);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDream(@PathVariable Long id, @RequestParam String username) {
        try {
            boolean gone = ds.deleteDream(id, username);
            if (gone) return ResponseEntity.ok("Dream deleted");
            else return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dream not found");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}
