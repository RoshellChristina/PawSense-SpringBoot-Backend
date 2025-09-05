package com.example.petapp.dto;

import lombok.Data;

@Data
public class PetPredictionRequest {
    private Long petId;                    // id of pet (we'll fetch static features)
    private String symptoms;               // semicolon-separated string "Cough;Lethargy"
    private Integer diseaseDurationDays;
    private String prediction;
    private Double probability;
}
