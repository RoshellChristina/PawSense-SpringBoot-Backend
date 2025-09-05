package com.example.petapp.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
public class PetDTO {
    private Long userId;
    private String name;
    private String breed;
    private String sex;
    private LocalDate dob;
    private Double weight;
    private String vaccinationStatus;
    private MultipartFile profilePic;
    private String notes;
    private List<String> healthTags;
}

