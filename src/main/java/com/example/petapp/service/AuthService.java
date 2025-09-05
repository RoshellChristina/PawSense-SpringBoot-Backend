// src/main/java/com/example/petapp/service/AuthService.java
package com.example.petapp.service;

import com.example.petapp.dto.AuthResponse;
import com.example.petapp.dto.LoginRequest;
import com.example.petapp.dto.SignupRequest;

public interface AuthService {
    AuthResponse login(LoginRequest req);
    void signup(SignupRequest req);
}
