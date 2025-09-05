// com/example/petapp/service/PetScoreService.java
package com.example.petapp.service;

import com.example.petapp.dao.PetScoreDAO;
import com.example.petapp.dao.PetDAO;
import com.example.petapp.model.Pet;
import com.example.petapp.model.PetScoreRequest;
import com.example.petapp.model.PetScoreResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PetScoreService {

    private static final Logger log = LoggerFactory.getLogger(PetScoreService.class);

    private final String ML_API_URL = "http://localhost:5000/predict_score"; // flask

    @Autowired
    private PetScoreDAO petScoreDAO;

    @Autowired
    private PetDAO petDAO;

    public PetScoreResponse predictAndSave(PetScoreRequest req) {
        // 1) Fetch static pet features
        Pet pet = petDAO.findById(req.getPetId());
        if (pet == null) {
            throw new IllegalArgumentException("Pet not found: " + req.getPetId());
        }

        // 2) compute AgeYears (fractional)
        Double ageYears = null;
        if (pet.getDob() != null) {
            long days = ChronoUnit.DAYS.between(pet.getDob(), LocalDate.now());
            ageYears = days / 365.25;
        } else {
            // fallback: try to infer from ageCategory (Puppy/Adult/Senior)
            String ac = pet.getAgeCategory();
            if ("Puppy".equalsIgnoreCase(ac)) ageYears = 1.0;
            else if ("Senior".equalsIgnoreCase(ac)) ageYears = 8.0;
            else ageYears = 4.0;
        }

        // 3) weight in KG (ensure not null)
        Double weightKg = pet.getWeight() == null ? 10.0 : pet.getWeight();

        // 4) Build payload with exact column names used during training
        Map<String, Object> payload = new HashMap<>();
        // categorical columns (keep exact names)
        payload.put("Breed", pet.getBreed() == null ? "" : pet.getBreed());
        payload.put("Size", pet.getSize() == null ? "" : pet.getSize());
        payload.put("AgeCategory", pet.getAgeCategory() == null ? "" : pet.getAgeCategory());
        payload.put("Sex", pet.getSex() == null ? "" : pet.getSex());
        payload.put("VaccinationStatus", pet.getVaccinationStatus() == null ? "" : pet.getVaccinationStatus());

        payload.put("HouseholdEnvironment", req.getHouseholdEnv());
        payload.put("SleepHours", req.getSleepHours());
        // numeric columns - exact casing expected by pipeline
        payload.put("Hurt", req.getHurt());
        payload.put("Hunger", req.getHunger());
        payload.put("Hydration", req.getHydration());
        payload.put("Hygiene", req.getHygiene());
        payload.put("Happiness", req.getHappiness());
        payload.put("Mobility", req.getMobility());

        // mapping MGD -> MoreGoodDays (exact name in pipeline)
        payload.put("MoreGoodDays", req.getMgd());

        // derived numeric features
        payload.put("AgeYears", ageYears);
        payload.put("WeightKg", weightKg);


        log.info("Payload to ML service: {}", payload);

        // 5) Call ML microservice
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> resp = restTemplate.postForObject(ML_API_URL, entity, Map.class);

        Double totalScore = ((Number) resp.get("total_score")).doubleValue();

        // 6) Save result
        return petScoreDAO.savePrediction(req, totalScore);
    }

    public List<com.example.petapp.model.PetScoreResponse> getHistory(Long petId) {
        return petScoreDAO.getHistory(petId);
    }
}
