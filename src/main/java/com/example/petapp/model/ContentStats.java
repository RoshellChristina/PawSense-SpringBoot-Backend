package com.example.petapp.model;

import lombok.Data;
@Data

public class ContentStats {
    private long likes;
    private long dislikes;
    private long comments;
    private long saves;
    private long views;
    private String myReaction; // "like" | "dislike" | null
    private boolean savedByMe;

}
