package com.example.petapp.dto;

public class UserSummary {
    public Long id;
    public String username;
    public String fullName;
    public String profilePictureBase64;

    public UserSummary() {}

    public UserSummary(Long id, String username, String fullName, String profilePictureBase64) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.profilePictureBase64 = profilePictureBase64;
    }
}