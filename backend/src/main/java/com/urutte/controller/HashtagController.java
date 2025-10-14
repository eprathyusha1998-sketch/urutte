package com.urutte.controller;

import com.urutte.model.Hashtag;
import com.urutte.repository.HashtagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hashtags")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://127.0.0.1:3000", "http://127.0.0.1:3001", "https://urutte.com", "https://www.urutte.com"})
public class HashtagController {

    @Autowired
    private HashtagRepository hashtagRepository;

    // Get hashtag suggestions based on partial input
    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getHashtagSuggestions(
            @RequestParam("q") String query,
            @RequestParam(value = "limit", defaultValue = "8") int limit,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        try {
            if (query == null || query.trim().isEmpty()) {
                // Return trending hashtags if no query
                List<Hashtag> trendingHashtags = hashtagRepository.findTrendingHashtags();
                List<String> suggestions = trendingHashtags.stream()
                    .limit(limit)
                    .map(Hashtag::getTag)
                    .collect(Collectors.toList());
                return ResponseEntity.ok(suggestions);
            }
            
            // Search for hashtags containing the query
            List<Hashtag> matchingHashtags = hashtagRepository.findByTagContaining(query.trim());
            List<String> suggestions = matchingHashtags.stream()
                .limit(limit)
                .map(Hashtag::getTag)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            System.err.println("Error getting hashtag suggestions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // Get trending hashtags
    @GetMapping("/trending")
    public ResponseEntity<List<String>> getTrendingHashtags(
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        try {
            List<Hashtag> trendingHashtags = hashtagRepository.findTrendingHashtags();
            List<String> suggestions = trendingHashtags.stream()
                .limit(limit)
                .map(Hashtag::getTag)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            System.err.println("Error getting trending hashtags: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // Get popular hashtags
    @GetMapping("/popular")
    public ResponseEntity<List<String>> getPopularHashtags(
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        try {
            List<Hashtag> popularHashtags = hashtagRepository.findPopularHashtags();
            List<String> suggestions = popularHashtags.stream()
                .limit(limit)
                .map(Hashtag::getTag)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            System.err.println("Error getting popular hashtags: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
