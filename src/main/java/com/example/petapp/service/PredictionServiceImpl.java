package com.example.petapp.service;

import com.example.petapp.dao.PetDAO;
import com.example.petapp.dao.PetPredictionDAO;
import com.example.petapp.dto.PetPredictionRequest;
import com.example.petapp.dto.PetPredictionResponse;
import com.example.petapp.model.Pet;
import com.example.petapp.model.PetSymptomPrediction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PredictionServiceImpl implements PredictionService {

    @Autowired
    private PetDAO petDAO;

    @Autowired
    private PetPredictionDAO petPredictionDAO;

    private final RestTemplate rest = new RestTemplate();
    private final String PREDICT_SERVICE_URL = "http://localhost:5000/predict_disease";

    @Override
    public PetPredictionResponse predictAndSave(PetPredictionRequest req) {
        Pet pet = petDAO.findById(req.getPetId());
        if (pet == null) {
            throw new RuntimeException("Pet not found with id " + req.getPetId());
        }

        // Build payload: static features from pet + non-static from request
        Map<String, Object> payload = new HashMap<>();
        payload.put("Breed", safeString(pet.getBreed()));
        payload.put("Sex", safeString(pet.getSex()));
        payload.put("VaccinationStatus", safeString(pet.getVaccinationStatus()));
        payload.put("DiseaseDurationDays", req.getDiseaseDurationDays() == null ? 0 : req.getDiseaseDurationDays());
        payload.put("Symptoms", safeString(req.getSymptoms()));

        // Derive AgeCategory and Size from pet fields
        payload.put("AgeCategory", deriveAgeCategory(pet.getDob()));
        payload.put("Size", deriveSizeCategory(pet.getWeight()));

        String prediction;
        Double prob;

        // If frontend already gave prediction & probability, use it.
        if (req.getPrediction() != null && req.getProbability() != null) {
            prediction = req.getPrediction();
            prob = req.getProbability();
        } else {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String,Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> resp = rest.postForEntity(PREDICT_SERVICE_URL, entity, Map.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                throw new RuntimeException("Prediction service error: " + resp.getStatusCode());
            }

            Map body = resp.getBody();
            prediction = (String) body.get("prediction");
            prob = Double.valueOf(body.get("probability").toString());
        }


        // persist to DB (only top prediction is stored)
        PetSymptomPrediction p = new PetSymptomPrediction();
        p.setPetId(pet.getId());
        p.setUserId(pet.getUserId());
        p.setSymptoms(req.getSymptoms());
        p.setDiseaseDurationDays(req.getDiseaseDurationDays());
        p.setPrediction(prediction);
        p.setPredictionProb(prob);

        PetSymptomPrediction saved = petPredictionDAO.save(p);

        // Build response DTO
        PetPredictionResponse out = new PetPredictionResponse();
        out.setId(saved.getId());
        out.setPetId(saved.getPetId());
        out.setUserId(saved.getUserId());
        out.setPrediction(saved.getPrediction());
        out.setProbability(saved.getPredictionProb());
        out.setSymptoms(saved.getSymptoms());
        out.setDiseaseDurationDays(saved.getDiseaseDurationDays());
        out.setCreatedAt(saved.getCreatedAt());

        return out;
    }

    private String safeString(String s) {
        return s == null ? "" : s;
    }

    private String deriveAgeCategory(java.time.LocalDate dob) {
        if (dob == null) return "Adult";
        java.time.Period age = java.time.Period.between(dob, java.time.LocalDate.now());
        int years = age.getYears();
        if (years < 2) return "Puppy";
        if (years < 7) return "Adult";
        return "Senior";
    }

    private String deriveSizeCategory(Double weightKg) {
        if (weightKg == null) return "Medium";
        if (weightKg < 10.0) return "Small";
        if (weightKg < 25.0) return "Medium";
        return "Large";
    }

    @Override
    public List<PetPredictionResponse> getPredictionsByPetId(Long petId) {
        List<PetSymptomPrediction> rows = petPredictionDAO.findByPetId(petId);
        return rows.stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    @Override
    public List<PetPredictionResponse> getPredictionsByUserId(Long userId) {
        // If you implemented petPredictionDAO.findByUserId(...) use it. Otherwise you can query pet ids and aggregate.
        List<com.example.petapp.model.PetSymptomPrediction> rows = petPredictionDAO.findByUserId(userId);
        return rows.stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    private PetPredictionResponse toResponseDto(com.example.petapp.model.PetSymptomPrediction p) {
        PetPredictionResponse out = new PetPredictionResponse();
        out.setId(p.getId());
        out.setPetId(p.getPetId());
        out.setUserId(p.getUserId());
        out.setPrediction(p.getPrediction());
        out.setProbability(p.getPredictionProb());
        out.setSymptoms(p.getSymptoms());
        out.setDiseaseDurationDays(p.getDiseaseDurationDays());
        out.setCreatedAt(p.getCreatedAt());
        return out;
    }
}
