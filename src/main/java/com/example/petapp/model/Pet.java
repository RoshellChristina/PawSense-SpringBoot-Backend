package com.example.petapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

@Data
public class Pet {
    private Long id;
    private Long userId;
    private String name;
    private String breed;
    private String sex;
    private LocalDate dob;
    private Double weight;
    private String vaccinationStatus;
    private byte[] profilePic;
    private LocalDateTime createdAt;
    private String notes;
    private List<String> healthTags;
    private LocalDateTime embeddingUpdatedAt;

    @JsonProperty("ageCategory")
    public String getAgeCategory() {
        if (this.dob == null) return "Adult";
        Period age = Period.between(this.dob, LocalDate.now());
        int years = age.getYears();
        if (years < 2) return "Puppy";
        if (years < 7) return "Adult";
        return "Senior";
    }

    @JsonProperty("size")
    public String getSize() {
        if (this.weight == null) return "Medium";
        double w = this.weight.doubleValue();
        if (w < 10.0) return "Small";
        if (w < 25.0) return "Medium";
        return "Large";
    }
}


