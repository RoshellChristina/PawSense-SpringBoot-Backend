package com.example.petapp.model;

import lombok.Data;

@Data
public class PetScoreRequest {
    private Long petId;
    private Double hurt;
    private Double hunger;
    private Double hydration;
    private Double hygiene;
    private Double happiness;
    private Double mobility;
    private Double mgd;
    private String householdEnv;
    private Double sleepHours;
}
