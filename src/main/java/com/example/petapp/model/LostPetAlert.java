package com.example.petapp.model;

import java.time.OffsetDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LostPetAlert {
    private Long id;
    private Double latitude;
    private Double longitude;
    private String lastSeenAddress;
    private Double radiusKm;
    private OffsetDateTime createdAt;
}
