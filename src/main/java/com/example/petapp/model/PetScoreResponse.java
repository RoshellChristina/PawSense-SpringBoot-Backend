package com.example.petapp.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PetScoreResponse {
    private Long id;
    private Long petId;
    private Double totalScore;
    private Double hurt;
    private Double hunger;
    private Double hydration;
    private Double hygiene;
    private Double happiness;
    private Double mobility;
    private Double mgd;
    private String householdEnv;
    private Double sleepHours;
    private LocalDateTime predictedAt;
}
