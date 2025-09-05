package com.example.petapp.model;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class PetSymptomPrediction {
    private Long id;
    private Long petId;
    private Long userId;
    private String symptoms;
    private Integer diseaseDurationDays;
    private String prediction;
    private Double predictionProb;
    private OffsetDateTime createdAt;
}
