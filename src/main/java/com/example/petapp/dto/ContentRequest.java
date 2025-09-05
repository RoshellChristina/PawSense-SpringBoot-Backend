package com.example.petapp.dto;

import java.util.List;

public class ContentRequest {
    public Long userId;
    public String contentType;
    public String title;
    public String body;
    public List<String> tags;

    // NEW: arrays of base64 blobs, types, and positions (1‑5)
    public List<String> mediaBase64List;
    public List<String> mediaTypeList;
    public List<Integer> positions;

    public Double lastSeenLat;
    public Double lastSeenLng;
    public String lastSeenAddress;
    public Double radiusKm;
}
