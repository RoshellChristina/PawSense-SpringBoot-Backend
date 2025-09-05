package com.example.petapp.dao;

import com.example.petapp.model.PetSymptomPrediction;

import java.util.List;

public interface PetPredictionDAO {
    PetSymptomPrediction save(PetSymptomPrediction p);
    List<PetSymptomPrediction> findByPetId(Long petId);
    List<PetSymptomPrediction> findByUserId(Long userId);
}
