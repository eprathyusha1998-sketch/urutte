package com.urutte.controller;

import com.urutte.model.Topic;
import com.urutte.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/topics")
@CrossOrigin(origins = "*")
public class TopicController {
    
    @Autowired
    private TopicService topicService;
    
    /**
     * Get available topics for the current user (excluding already liked ones)
     */
    @GetMapping("/available")
    public ResponseEntity<List<Topic>> getAvailableTopics(Authentication authentication) {
        String userId = authentication.getName();
        List<Topic> topics = topicService.getAvailableTopicsForUser(userId);
        return ResponseEntity.ok(topics);
    }
    
    /**
     * Get random topics for suggestion (excluding already liked ones)
     */
    @GetMapping("/suggestions")
    public ResponseEntity<List<Topic>> getTopicSuggestions(
            Authentication authentication,
            @RequestParam(defaultValue = "5") int limit) {
        String userId = authentication.getName();
        List<Topic> topics = topicService.getRandomTopicsForUser(userId, limit);
        return ResponseEntity.ok(topics);
    }
    
    /**
     * Get user's liked topics
     */
    @GetMapping("/liked")
    public ResponseEntity<List<Topic>> getLikedTopics(Authentication authentication) {
        String userId = authentication.getName();
        List<Topic> topics = topicService.getUserLikedTopics(userId);
        return ResponseEntity.ok(topics);
    }
    
    /**
     * Like a topic
     */
    @PostMapping("/{topicId}/like")
    public ResponseEntity<Map<String, Object>> likeTopic(
            @PathVariable String topicId,
            Authentication authentication) {
        String userId = authentication.getName();
        boolean success = topicService.likeTopic(userId, topicId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "Topic liked successfully" : "Failed to like topic");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Unlike a topic
     */
    @DeleteMapping("/{topicId}/like")
    public ResponseEntity<Map<String, Object>> unlikeTopic(
            @PathVariable String topicId,
            Authentication authentication) {
        String userId = authentication.getName();
        boolean success = topicService.unlikeTopic(userId, topicId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "Topic unliked successfully" : "Failed to unlike topic");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if user likes a topic
     */
    @GetMapping("/{topicId}/liked")
    public ResponseEntity<Map<String, Boolean>> isTopicLiked(
            @PathVariable String topicId,
            Authentication authentication) {
        String userId = authentication.getName();
        boolean isLiked = topicService.isTopicLikedByUser(userId, topicId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("isLiked", isLiked);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all active topics (public endpoint)
     */
    @GetMapping("/all")
    public ResponseEntity<List<Topic>> getAllTopics() {
        List<Topic> topics = topicService.getAllActiveTopics();
        return ResponseEntity.ok(topics);
    }
    
    /**
     * Initialize default topics (for admin purposes)
     */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, String>> initializeTopics() {
        topicService.initializeDefaultTopics();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Default topics initialized successfully");
        
        return ResponseEntity.ok(response);
    }
}
