package com.example.petapp.model;

import java.time.LocalDateTime;

public class MoodRecord {
    private Long id;
    private Long petId;
    private String emotion;
    private Double confidence;
    private byte[] image;
    private LocalDateTime recordedAt;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public String getEmotion() { return emotion; }
    public void setEmotion(String emotion) { this.emotion = emotion; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}