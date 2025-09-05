package com.example.petapp.dto;

import java.util.List;

public class UserProfileUpdateRequest {
    private String fullname;
    private String email;
    private String bio;
    private String accountStatus;
    private String profilePictureBase64;
    private List<AddressUpdateRequest> addresses;
    private String businessType;// ← New field


    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public String getProfilePictureBase64() { return profilePictureBase64; }
    public void setProfilePictureBase64(String profilePictureBase64) { this.profilePictureBase64 = profilePictureBase64; }

    public List<AddressUpdateRequest> getAddresses() {
        return addresses;
    }
    public void setAddresses(List<AddressUpdateRequest> addresses) {
        this.addresses = addresses;
    }
}
