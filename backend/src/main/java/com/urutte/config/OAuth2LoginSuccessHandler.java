package com.urutte.config;

import com.urutte.model.User;
import com.urutte.repository.UserRepository;
import com.urutte.service.ProfilePictureService;
import com.urutte.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private ProfilePictureService profilePictureService;

    @Value("${app.frontend.url:http://localhost}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = oAuth2User.getAttributes();
            
            // Extract user info from OAuth2 response
            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");
            String picture = (String) attributes.get("picture");
            String googleId = (String) attributes.get("sub");
            String givenName = (String) attributes.get("given_name");
            String familyName = (String) attributes.get("family_name");
            Boolean emailVerified = (Boolean) attributes.get("email_verified");
            
            System.out.println("OAuth2 Login Success - Email: " + email + ", Name: " + name);
        
            // Find or create user
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setId(java.util.UUID.randomUUID().toString());
                        newUser.setEmail(email);
                        newUser.setName(name);
                        newUser.setPicture(profilePictureService.generateDefaultAvatar(newUser.getId()));
                        newUser.setGoogleId(googleId);
                        
                        // Generate username from email (before @)
                        String username = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                        // Ensure uniqueness by appending random number if exists
                        String baseUsername = username;
                        int counter = 1;
                        while (userRepository.findByUsername(username).isPresent()) {
                            username = baseUsername + counter++;
                        }
                        newUser.setUsername(username);
                        
                        // Set email verified if Google says so
                        if (emailVerified != null && emailVerified) {
                            newUser.setEmailVerifiedAt(java.time.Instant.now());
                        }
                        
                        System.out.println("Creating new user: " + newUser.getEmail());
                        return userRepository.save(newUser);
                    });
            
            // Update user info if changed
            boolean needsUpdate = false;
            if (!name.equals(user.getName())) {
                user.setName(name);
                needsUpdate = true;
            }
            // Skip updating picture from Google - use default avatars instead
            if (googleId != null && !googleId.equals(user.getGoogleId())) {
                user.setGoogleId(googleId);
                needsUpdate = true;
            }
            
            // Always update last login time
            user.setLastLoginAt(java.time.Instant.now());
            needsUpdate = true;
            
            if (needsUpdate) {
                userRepository.save(user);
                System.out.println("Updated user: " + user.getEmail());
            }
            
            // Generate JWT token
            String token = jwtUtil.generateToken(user.getId(), user.getEmail());
            
            // Redirect to frontend with token
            String redirectUrl = frontendUrl + "/?token=" + token;
            System.out.println("Redirecting to: " + redirectUrl);
            
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
            
        } catch (Exception e) {
            System.err.println("OAuth2 Login Error: " + e.getMessage());
            e.printStackTrace();
            
            // Redirect to frontend with error
            String errorUrl = frontendUrl + "/?error=true&message=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8");
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }
    
}

