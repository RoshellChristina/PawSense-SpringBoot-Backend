package com.example.petapp.service;

import com.example.petapp.dto.PetPredictionRequest;
import com.example.petapp.dto.PetPredictionResponse;

import java.util.List;

public interface PredictionService {
    PetPredictionResponse predictAndSave(PetPredictionRequest req);
    List<PetPredictionResponse> getPredictionsByPetId(Long petId);
    List<PetPredictionResponse> getPredictionsByUserId(Long userId);
}
