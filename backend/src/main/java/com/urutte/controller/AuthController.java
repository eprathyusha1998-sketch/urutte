package com.urutte.controller;

import com.urutte.dto.LoginRequest;
import com.urutte.dto.RegisterRequest;
import com.urutte.dto.AuthResponse;
import com.urutte.model.User;
import com.urutte.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost", "http://localhost:80", "http://frontend", "https://urutte.com", "https://www.urutte.com"})
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                AuthResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build()
            );
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                AuthResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build()
            );
        }
    }
}
