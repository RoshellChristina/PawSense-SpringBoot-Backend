// src/dto/PetResponseDTO.java
package com.example.petapp.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PetResponseDTO {
    private Long id;
    private Long userId;
    private String name;
    private String breed;
    private String sex;
    private LocalDate dob;
    private Double weight;
    private String vaccinationStatus;
    private String profilePicBase64;
    private LocalDateTime createdAt;
    private String notes;
    private List<String> healthTags;
    private LocalDateTime embeddingUpdatedAt;
}
