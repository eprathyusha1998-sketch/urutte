package com.urutte.controller;

import com.urutte.model.AiAdmin;
import com.urutte.model.Topic;
import com.urutte.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-content")
@CrossOrigin(origins = "*")
public class AiContentController {
    
    @Autowired
    private AiAdminService aiAdminService;
    
    @Autowired
    private TopicService topicService;
    
    @Autowired
    private ContentGenerationService contentGenerationService;
    
    @Autowired
    private AiContentScheduler aiContentScheduler;
    
    // AI Admin endpoints
    
    @GetMapping("/ai-admin")
    public ResponseEntity<AiAdmin> getActiveAiAdmin() {
        return aiAdminService.getActiveAiAdmin()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/ai-admin")
    public ResponseEntity<AiAdmin> createAiAdmin(@RequestBody CreateAiAdminRequest request) {
        try {
            AiAdmin aiAdmin = aiAdminService.createAiAdmin(
                request.getName(),
                request.getUsername(),
                request.getEmail(),
                request.getBio(),
                request.getAvatarUrl()
            );
            return ResponseEntity.ok(aiAdmin);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/ai-admin/{id}")
    public ResponseEntity<AiAdmin> updateAiAdmin(@PathVariable Long id, @RequestBody UpdateAiAdminRequest request) {
        try {
            AiAdmin aiAdmin = aiAdminService.updateAiAdmin(id, request.getName(), request.getBio(), request.getAvatarUrl());
            return ResponseEntity.ok(aiAdmin);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/ai-admin/{id}/toggle")
    public ResponseEntity<AiAdmin> toggleAiAdminStatus(@PathVariable Long id) {
        try {
            AiAdmin aiAdmin = aiAdminService.toggleAiAdminStatus(id);
            return ResponseEntity.ok(aiAdmin);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/ai-admin/stats")
    public ResponseEntity<Map<String, Object>> getAiAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        
        aiAdminService.getActiveAiAdmin().ifPresent(aiAdmin -> {
            stats.put("activeAiAdmin", aiAdminService.getAiAdminStats(aiAdmin.getId()));
        });
        
        stats.put("totalTopics", topicService.getAllActiveTopics().size());
        stats.put("categories", topicService.getAllCategories());
        
        return ResponseEntity.ok(stats);
    }
    
    // Topic endpoints
    
    @GetMapping("/topics")
    public ResponseEntity<List<Topic>> getAllTopics() {
        return ResponseEntity.ok(topicService.getAllActiveTopics());
    }
    
    @GetMapping("/topics/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(topicService.getAllCategories());
    }
    
    @GetMapping("/topics/category/{category}")
    public ResponseEntity<List<Topic>> getTopicsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(topicService.getTopicsByCategory(category));
    }
    
    @GetMapping("/topics/search")
    public ResponseEntity<List<Topic>> searchTopics(@RequestParam String query) {
        return ResponseEntity.ok(topicService.searchTopics(query));
    }
    
    @PostMapping("/topics")
    public ResponseEntity<Topic> createTopic(@RequestBody CreateTopicRequest request) {
        Topic topic = topicService.createTopic(
            request.getName(),
            request.getDescription(),
            request.getCategory(),
            request.getKeywords(),
            request.getSearchQueries()
        );
        return ResponseEntity.ok(topic);
    }
    
    @PutMapping("/topics/{id}")
    public ResponseEntity<Topic> updateTopic(@PathVariable Long id, @RequestBody UpdateTopicRequest request) {
        try {
            Topic topic = topicService.updateTopic(
                id,
                request.getName(),
                request.getDescription(),
                request.getCategory(),
                request.getKeywords(),
                request.getSearchQueries(),
                request.getPriority(),
                request.getThreadsPerRun()
            );
            return ResponseEntity.ok(topic);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/topics/{id}/toggle")
    public ResponseEntity<Topic> toggleTopicStatus(@PathVariable Long id) {
        try {
            Topic topic = topicService.toggleTopicStatus(id);
            return ResponseEntity.ok(topic);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/topics/{id}/stats")
    public ResponseEntity<TopicService.TopicStats> getTopicStats(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(topicService.getTopicStats(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Content generation endpoints
    
    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateContentNow() {
        try {
            aiContentScheduler.generateContentNow();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Content generation started successfully");
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error starting content generation: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/generate/topic/{id}")
    public ResponseEntity<Map<String, String>> generateContentForTopic(@PathVariable Long id) {
        try {
            aiAdminService.getActiveAiAdmin().ifPresent(aiAdmin -> {
                topicService.getTopicById(id).ifPresent(topic -> {
                    contentGenerationService.generateContentForTopic(topic, aiAdmin);
                });
            });
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Content generation started for topic");
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error generating content for topic: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, String>> initializeSystem() {
        try {
            aiAdminService.ensureAiAdminExists();
            topicService.initializeDefaultTopics();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "AI content system initialized successfully");
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error initializing system: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // Request DTOs
    
    public static class CreateAiAdminRequest {
        private String name;
        private String username;
        private String email;
        private String bio;
        private String avatarUrl;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }
        
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    }
    
    public static class UpdateAiAdminRequest {
        private String name;
        private String bio;
        private String avatarUrl;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }
        
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    }
    
    public static class CreateTopicRequest {
        private String name;
        private String description;
        private String category;
        private String keywords;
        private String searchQueries;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getKeywords() { return keywords; }
        public void setKeywords(String keywords) { this.keywords = keywords; }
        
        public String getSearchQueries() { return searchQueries; }
        public void setSearchQueries(String searchQueries) { this.searchQueries = searchQueries; }
    }
    
    public static class UpdateTopicRequest {
        private String name;
        private String description;
        private String category;
        private String keywords;
        private String searchQueries;
        private Integer priority;
        private Integer threadsPerRun;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getKeywords() { return keywords; }
        public void setKeywords(String keywords) { this.keywords = keywords; }
        
        public String getSearchQueries() { return searchQueries; }
        public void setSearchQueries(String searchQueries) { this.searchQueries = searchQueries; }
        
        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }
        
        public Integer getThreadsPerRun() { return threadsPerRun; }
        public void setThreadsPerRun(Integer threadsPerRun) { this.threadsPerRun = threadsPerRun; }
    }
}
