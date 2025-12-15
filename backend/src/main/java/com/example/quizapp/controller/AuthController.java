package com.example.quizapp.controller;

import com.example.quizapp.dto.AuthResponse;
import com.example.quizapp.dto.LoginRequest;
import com.example.quizapp.dto.ProfileResponse;
import com.example.quizapp.dto.RegisterRequest;
import com.example.quizapp.dto.UpdateProfileRequest;
import com.example.quizapp.service.AuthService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> profile(Principal principal) {
        return ResponseEntity.ok(authService.getProfile(principal.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request,
                                                         Principal principal) {
        return ResponseEntity.ok(authService.updateProfile(principal.getName(), request));
    }
}
