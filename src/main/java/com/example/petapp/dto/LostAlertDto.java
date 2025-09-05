package com.example.petapp.dto;

import java.time.OffsetDateTime;
import lombok.Data;
@Data

public class LostAlertDto {
    public Long id;
    public String title;
    public String thumbnailBase64;
    public String media1Type;
    public Double lastSeenLat;
    public Double lastSeenLng;
    public String lastSeenAddress;
    public OffsetDateTime createdAt;
    public UserSummary user;
}