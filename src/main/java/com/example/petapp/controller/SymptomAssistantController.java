package com.example.petapp.controller;

import com.example.petapp.service.SymptomAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/symptom_assist")
public class SymptomAssistantController {

    @Autowired
    private SymptomAssistantService assistantService;

    @PostMapping("/assist")
    public ResponseEntity<?> assist(@RequestBody Map<String,String> body) {
        String userText = body.get("text");
        if (userText == null || userText.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "text is required"));
        }
        try {
            Map<String,Object> out = assistantService.suggestSymptoms(userText);
            return ResponseEntity.ok(out);
        } catch (Exception ex) {
            // log and return safe error
            log.error("assist failed", ex);
            return ResponseEntity.status(500).body(Map.of("message", "assist failed"));
        }
    }
}
