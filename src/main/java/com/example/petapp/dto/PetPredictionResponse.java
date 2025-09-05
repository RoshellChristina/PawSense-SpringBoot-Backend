package com.example.petapp.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class PetPredictionResponse {
    private Long id;
    private Long petId;
    private Long userId;
    private String prediction;
    private Double probability;
    private String symptoms;
    private Integer diseaseDurationDays;
    private OffsetDateTime createdAt;
}
