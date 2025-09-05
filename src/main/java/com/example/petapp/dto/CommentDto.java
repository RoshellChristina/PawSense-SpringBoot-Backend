package com.example.petapp.dto;

import java.time.OffsetDateTime;
import lombok.Data;
@Data

public class CommentDto {
    private Long id;
    private Long contentId;
    private Long userId;
    private String username;
    private String body;
    private OffsetDateTime createdAt;
}
