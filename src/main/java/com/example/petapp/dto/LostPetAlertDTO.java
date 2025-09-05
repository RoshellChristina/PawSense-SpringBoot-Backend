// src/main/java/com/example/petapp/dto/LostPetAlertDTO.java
package com.example.petapp.dto;

import java.time.OffsetDateTime;

public class LostPetAlertDTO {
    public Long id;
    public Double latitude;
    public Double longitude;
    public String lastSeenAddress;
    public Double radiusKm;
    public OffsetDateTime createdAt;
}
