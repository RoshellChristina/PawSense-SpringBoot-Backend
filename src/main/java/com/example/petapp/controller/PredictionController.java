package com.example.petapp.controller;

import com.example.petapp.dto.PetPredictionRequest;
import com.example.petapp.dto.PetPredictionResponse;
import com.example.petapp.model.Pet;
import com.example.petapp.service.DiseaseAdviceService;
import com.example.petapp.service.MoodRecordService;
import com.example.petapp.service.PetAdviceService;
import com.example.petapp.service.PredictionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PredictionController.class);

    private final DiseaseAdviceService adviceService;

    @Autowired
    public PredictionController(DiseaseAdviceService adviceService) {
        this.adviceService = adviceService;
    }

    @Autowired
    private PredictionService predictionService;

    @Autowired
    private com.example.petapp.dao.PetDAO petDAO;

    @PostMapping
    public ResponseEntity<PetPredictionResponse> predictAndSave(@RequestBody PetPredictionRequest req) {
        PetPredictionResponse res = predictionService.predictAndSave(req);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/pet/{petId}")
    public ResponseEntity<List<PetPredictionResponse>> getByPetId(@PathVariable Long petId) {
        List<PetPredictionResponse> list = predictionService.getPredictionsByPetId(petId);
        return ResponseEntity.ok(list);
    }

    // NEW: fetch predictions for a user (optional)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PetPredictionResponse>> getByUserId(@PathVariable Long userId) {
        List<PetPredictionResponse> list = predictionService.getPredictionsByUserId(userId);
        return ResponseEntity.ok(list);
    }

    // === Updated to use getInputStream() so it works inside JARs ===
    @GetMapping("/symptoms")
    public ResponseEntity<List<String>> getSymptoms() {
        try {
            ClassPathResource resource = new ClassPathResource("disease_prediction/symptoms.json");
            try (java.io.InputStream is = resource.getInputStream()) {
                String json = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                List<String> list = mapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
                return ResponseEntity.ok(list);
            }
        } catch (IOException ex) {
            log.error("Failed to read symptoms.json", ex);
            return ResponseEntity.status(500).build();
        }
    }

    // === Minimal, robust pet info endpoint - returns Pet so Jackson includes derived fields ===
    @GetMapping("/petinfo/{petId}")
    public ResponseEntity<Pet> getPetInfo(@PathVariable Long petId) {
        log.info("GET /api/predictions/petinfo/{} called", petId);
        Pet pet = petDAO.findById(petId);
        if (pet == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(pet);
    }

    @GetMapping("/advice/{disease}")
    public ResponseEntity<Map<String,String>> getAdvice(@PathVariable String disease) {
        String advice = adviceService.generateAdvice(disease);
        return ResponseEntity.ok(Map.of("advice", advice));
    }


}
