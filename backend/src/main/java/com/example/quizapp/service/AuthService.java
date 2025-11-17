package com.example.quizapp.service;

import com.example.quizapp.dto.AuthResponse;
import com.example.quizapp.dto.LoginRequest;
import com.example.quizapp.dto.ProfileResponse;
import com.example.quizapp.dto.RegisterRequest;
import com.example.quizapp.dto.UpdateProfileRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    ProfileResponse getProfile(String email);
    ProfileResponse updateProfile(String email, UpdateProfileRequest request);
}
