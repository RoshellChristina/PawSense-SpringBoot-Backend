package com.example.petapp.controller;

import com.example.petapp.model.Content;
import com.example.petapp.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Autowired
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/pet/{petId}")
    public ResponseEntity<List<Content>> recommendForPet(
            @PathVariable("petId") Long petId,
            @RequestParam(value = "businessOnly", required = false, defaultValue = "true") boolean businessOnly,
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(value = "requestingUserId", required = false) Long requestingUserId
    ) {
        List<Content> recs = recommendationService.recommendForPet(petId, businessOnly, requestingUserId, limit);
        return ResponseEntity.ok(recs);
    }
}
