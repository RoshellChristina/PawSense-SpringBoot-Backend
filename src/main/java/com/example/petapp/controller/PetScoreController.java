package com.example.petapp.controller;

import com.example.petapp.model.PetScoreRequest;
import com.example.petapp.model.PetScoreResponse;
import com.example.petapp.service.PetScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pet-score")
public class PetScoreController {

    @Autowired
    private PetScoreService petScoreService;

    // Predict (calls Python service, saves to DB, returns saved object)
    @PostMapping("/predict")
    public ResponseEntity<PetScoreResponse> predictScore(@RequestBody PetScoreRequest request) {
        PetScoreResponse saved = petScoreService.predictAndSave(request);
        return ResponseEntity.ok(saved);
    }

    // History (used by React)
    @GetMapping("/history/{petId}")
    public ResponseEntity<List<PetScoreResponse>> getHistory(@PathVariable Long petId) {
        List<PetScoreResponse> history = petScoreService.getHistory(petId);
        return ResponseEntity.ok(history);
    }
}
