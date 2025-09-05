// UserService.java
package com.example.petapp.service;

import com.example.petapp.dto.UserDTO;
import com.example.petapp.dto.UserProfileUpdateRequest;

import java.util.List;

public interface UserService {
    UserDTO getUserProfile(Long userId);
    void updateUserProfile(Long userId, UserProfileUpdateRequest req);
    List<String> getBusinessTypes();

    List<UserDTO> listBusinesses(String businessType, Integer limit, Integer offset);
}

