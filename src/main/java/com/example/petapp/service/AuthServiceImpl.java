package com.example.petapp.service;

import com.example.petapp.dao.UserDao;
import com.example.petapp.dto.AuthResponse;
import com.example.petapp.dto.LoginRequest;
import com.example.petapp.dto.SignupRequest;
import com.example.petapp.dto.UserDTO;
import com.example.petapp.model.User;
import com.example.petapp.util.JwtUtil;
import com.example.petapp.util.PasswordUtil;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserDao userDao;
    private final PasswordUtil pwdUtil;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserDao userDao, PasswordUtil pwdUtil, JwtUtil jwtUtil) {
        this.userDao = userDao;
        this.pwdUtil = pwdUtil;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public AuthResponse login(LoginRequest req) {
        User user = userDao.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!pwdUtil.matches(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        if ("blocked".equals(user.getAccountStatus())) {
            throw new RuntimeException("Account blocked");
        }

        // Generate JWT using user details
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // Map entity to DTO
        UserDTO userDto = new UserDTO(user.getId(), user.getUsername(), user.getRole());

        // Return both token & user info
        return new AuthResponse(token, userDto);
    }

    @Override
    public void signup(SignupRequest req) {
        if (userDao.findByUsername(req.getUsername()).isPresent())
            throw new RuntimeException("Username already taken");
        if (userDao.findByEmail(req.getEmail()).isPresent())
            throw new RuntimeException("Email already in use");

        User u = new User();
        u.setFullname(req.getFullname());
        u.setUsername(req.getUsername());
        u.setEmail(req.getEmail());
        u.setPasswordHash(pwdUtil.hash(req.getPassword()));
        u.setRole(req.getRole());
        u.setAccountStatus("public");
        u.setBusinessType(req.getBusinessType());
        userDao.save(u);
    }
}