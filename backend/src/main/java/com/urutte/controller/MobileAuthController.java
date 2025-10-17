package com.urutte.controller;

import com.urutte.model.User;
import com.urutte.repository.UserRepository;
import com.urutte.service.ProfilePictureService;
import com.urutte.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class MobileAuthController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private ProfilePictureService profilePictureService;
    
    @Autowired
    private RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @PostMapping("/google-mobile")
    public ResponseEntity<?> handleGoogleMobileAuth(@RequestBody Map<String, String> request) {
        try {
            String code = request.get("code");
            String redirectUri = request.get("redirectUri");
            
            if (code == null || redirectUri == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing code or redirectUri"));
            }
            
            // Exchange authorization code for access token
            String tokenUrl = "https://oauth2.googleapis.com/token";
            String tokenRequestBody = String.format(
                "client_id=%s&client_secret=%s&code=%s&grant_type=authorization_code&redirect_uri=%s",
                clientId, clientSecret, code, redirectUri
            );
            
            // Make request to Google token endpoint
            String tokenResponse = restTemplate.postForObject(tokenUrl, tokenRequestBody, String.class);
            
            // Parse the response to get access token
            // Note: In production, use a proper JSON parser
            String accessToken = extractAccessToken(tokenResponse);
            
            if (accessToken == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Failed to get access token"));
            }
            
            // Get user info from Google
            String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken;
            Map<String, Object> userInfo = restTemplate.getForObject(userInfoUrl, Map.class);
            
            // Extract user information
            String email = (String) userInfo.get("email");
            String name = (String) userInfo.get("name");
            String picture = (String) userInfo.get("picture");
            String googleId = (String) userInfo.get("id");
            Boolean emailVerified = (Boolean) userInfo.get("verified_email");
            
            // Find or create user
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setId(java.util.UUID.randomUUID().toString());
                        newUser.setEmail(email);
                        newUser.setName(name);
                        newUser.setPicture(profilePictureService.generateDefaultAvatar(newUser.getId()));
                        newUser.setGoogleId(googleId);
                        
                        // Generate username from email
                        String username = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                        String baseUsername = username;
                        int counter = 1;
                        while (userRepository.findByUsername(username).isPresent()) {
                            username = baseUsername + counter++;
                        }
                        newUser.setUsername(username);
                        
                        if (emailVerified != null && emailVerified) {
                            newUser.setEmailVerifiedAt(java.time.Instant.now());
                        }
                        
                        return userRepository.save(newUser);
                    });
            
            // Update user info if changed
            boolean needsUpdate = false;
            if (!name.equals(user.getName())) {
                user.setName(name);
                needsUpdate = true;
            }
            if (googleId != null && !googleId.equals(user.getGoogleId())) {
                user.setGoogleId(googleId);
                needsUpdate = true;
            }
            
            user.setLastLoginAt(java.time.Instant.now());
            needsUpdate = true;
            
            if (needsUpdate) {
                userRepository.save(user);
            }
            
            // Generate JWT token
            String token = jwtUtil.generateToken(user.getId(), user.getEmail());
            
            // Return user data and token
            return ResponseEntity.ok(Map.of(
                "token", token,
                "user", Map.of(
                    "id", user.getId(),
                    "name", user.getName(),
                    "email", user.getEmail(),
                    "username", user.getUsername(),
                    "picture", user.getPicture(),
                    "followersCount", user.getFollowersCount(),
                    "followingCount", user.getFollowingCount(),
                    "createdAt", user.getCreatedAt().toString()
                )
            ));
            
        } catch (Exception e) {
            System.err.println("Mobile OAuth error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Authentication failed"));
        }
    }
    
    private String extractAccessToken(String response) {
        try {
            // Simple JSON parsing - in production use a proper JSON library
            if (response.contains("\"access_token\"")) {
                int start = response.indexOf("\"access_token\":\"") + 16;
                int end = response.indexOf("\"", start);
                return response.substring(start, end);
            }
        } catch (Exception e) {
            System.err.println("Error extracting access token: " + e.getMessage());
        }
        return null;
    }
}
