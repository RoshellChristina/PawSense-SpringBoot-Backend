// src/main/java/com/example/petapp/dto/AddressUpdateRequest.java
package com.example.petapp.dto;

public class AddressUpdateRequest {
    private Long id;           // null → new
    private String address;
    private Double latitude;
    private Double longitude;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
