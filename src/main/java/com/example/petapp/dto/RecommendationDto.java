package com.example.petapp.dto;

import lombok.Data;
import java.util.List;

@Data
public class RecommendationDto {
    private Long contentId;
    private Long userId;       // author id
    private String username;   // author username
    private String title;
    private String body;
    private List<String> tags;
    private Double distance;

    public RecommendationDto() {}

    public RecommendationDto(Long contentId, Long userId, String username, String title, String body, List<String> tags, Double distance) {
        this.contentId = contentId;
        this.userId = userId;
        this.username = username;
        this.title = title;
        this.body = body;
        this.tags = tags;
        this.distance = distance;
    }
}
