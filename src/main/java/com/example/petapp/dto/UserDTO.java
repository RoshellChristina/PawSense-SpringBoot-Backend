// src/main/java/com/example/petapp/dto/UserDTO.java
package com.example.petapp.dto;

import java.util.List;

public class UserDTO {
    private Long id;
    private String username;
    private String role;
    private String fullname;
    private String email;
    private String bio;
    private String accountStatus;
    private String profilePictureBase64;
    private List<AddressDTO> addresses;
    private String businessType;

    public UserDTO() {}

    public UserDTO(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public String getFullname() {
        return fullname;
    }
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAccountStatus() {
        return accountStatus;
    }
    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getProfilePictureBase64() {
        return profilePictureBase64;
    }
    public void setProfilePictureBase64(String profilePictureBase64) {
        this.profilePictureBase64 = profilePictureBase64;
    }

    public List<AddressDTO> getAddresses() {
        return addresses;
    }
    public void setAddresses(List<AddressDTO> addresses) {
        this.addresses = addresses;
    }

}
