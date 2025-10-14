package com.urutte.service;

import com.urutte.dto.AuthResponse;
import com.urutte.dto.LoginRequest;
import com.urutte.dto.RegisterRequest;
import com.urutte.dto.UserDto;
import com.urutte.model.User;
import com.urutte.repository.UserRepository;
import com.urutte.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public AuthResponse register(RegisterRequest request) {
        // Validate password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with this email already exists");
        }
        
        // Create new user
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName(request.getFirstName() + " " + request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(java.time.Instant.now());
        user.setUpdatedAt(java.time.Instant.now());
        
        // Save user
        user = userRepository.save(user);
        
        // Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        
        // Convert to DTO
        UserDto userDto = userService.convertToDto(user, user.getId());
        
        return AuthResponse.builder()
            .success(true)
            .message("Registration successful")
            .token(token)
            .user(userDto)
            .build();
    }
    
    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        
        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        
        // Update last login
        user.setLastLoginAt(java.time.Instant.now());
        userRepository.save(user);
        
        // Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        
        // Convert to DTO
        UserDto userDto = userService.convertToDto(user, user.getId());
        
        return AuthResponse.builder()
            .success(true)
            .message("Login successful")
            .token(token)
            .user(userDto)
            .build();
    }
    
}
