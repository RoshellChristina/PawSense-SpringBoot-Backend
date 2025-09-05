package com.example.petapp.model;

import com.example.petapp.util.ImageUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Content {
    private Long id;
    private Long userId;
    private String contentType;
    private Long lostPetAlertId;
    private String title;
    private String body;
    private List<String> tags;
    private OffsetDateTime createdAt;
    private Double[] embedding;


    User user;

    // raw blobs — we don’t want Jackson to try to write these
    @JsonIgnore private byte[] media1;
    @JsonIgnore private byte[] media2;
    @JsonIgnore private byte[] media3;
    @JsonIgnore private byte[] media4;
    @JsonIgnore private byte[] media5;

    // types you might still want to send
    private String media1Type;
    private String media2Type;
    private String media3Type;
    private String media4Type;
    private String media5Type;


    // transient base64 fields for JSON I/O
    private transient String media1Base64;
    private transient String media2Base64;
    private transient String media3Base64;
    private transient String media4Base64;
    private transient String media5Base64;

    // After fetching from DB, populate base64 for JSON responses
    public void encodeMediaToBase64() {
        if (media1 != null) media1Base64 = ImageUtil.encodeToBase64(media1);
        if (media2 != null) media2Base64 = ImageUtil.encodeToBase64(media2);
        if (media3 != null) media3Base64 = ImageUtil.encodeToBase64(media3);
        if (media4 != null) media4Base64 = ImageUtil.encodeToBase64(media4);
        if (media5 != null) media5Base64 = ImageUtil.encodeToBase64(media5);
    }

    // Before saving, decode any base64 into raw bytes
    public void decodeBase64ToMedia() {
        if (media1Base64 != null) media1 = ImageUtil.decodeFromBase64(media1Base64);
        if (media2Base64 != null) media2 = ImageUtil.decodeFromBase64(media2Base64);
        if (media3Base64 != null) media3 = ImageUtil.decodeFromBase64(media3Base64);
        if (media4Base64 != null) media4 = ImageUtil.decodeFromBase64(media4Base64);
        if (media5Base64 != null) media5 = ImageUtil.decodeFromBase64(media5Base64);
    }


    // inside Content.java (add these lines near other fields)
    private Double distance; // optional similarity/distance returned by NN queries

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }


    // convenience author fields
    private Long authorId;
    private String authorUsername;
}
