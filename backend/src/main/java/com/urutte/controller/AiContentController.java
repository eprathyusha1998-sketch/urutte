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
    
    @Autowired
    private IndiaContentScheduler indiaContentScheduler;
    
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
    public ResponseEntity<Topic> updateTopic(@PathVariable String id, @RequestBody UpdateTopicRequest request) {
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
    public ResponseEntity<Topic> toggleTopicStatus(@PathVariable String id) {
        try {
            Topic topic = topicService.toggleTopicStatus(id);
            return ResponseEntity.ok(topic);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/topics/{id}/stats")
    public ResponseEntity<TopicService.TopicStats> getTopicStats(@PathVariable String id) {
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
    public ResponseEntity<Map<String, String>> generateContentForTopic(@PathVariable String id) {
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
    
    @PostMapping("/generate/india")
    public ResponseEntity<Map<String, String>> generateIndiaContentNow() {
        try {
            indiaContentScheduler.generateIndiaContentNow();
            Map<String, String> response = new HashMap<>();
            response.put("message", "India content generation started successfully");
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error starting India content generation: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/topics/update-all-to-5-posts")
    public ResponseEntity<Map<String, String>> updateAllTopicsTo5Posts() {
        try {
            List<Topic> allTopics = topicService.getAllActiveTopics();
            int updated = 0;
            
            for (Topic topic : allTopics) {
                topic.setThreadsPerRun(5);
                topicService.updateTopic(
                    topic.getId(),
                    topic.getName(),
                    topic.getDescription(),
                    topic.getAiPrompt(),
                    topic.getCategory(),
                    topic.getKeywords(),
                    topic.getPriority(),
                    5 // Set to 5 posts per run
                );
                updated++;
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Updated " + updated + " topics to generate 5 posts per run");
            response.put("status", "success");
            response.put("updated", String.valueOf(updated));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error updating topics: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/topics/create-india-topics")
    public ResponseEntity<Map<String, String>> createIndiaTopics() {
        try {
            // Create India-focused topics with high priority for hourly generation
            String[][] indiaTopics = {
                {"India News", "Latest news and updates from across India", "Generate content about major Indian news stories, political developments, economic updates, and significant events across India", "News", "India news,Indian politics,Indian economy,breaking news India", "1", "5"},
                {"Tamil Nadu News", "Latest news and updates from Tamil Nadu state", "Generate content about Tamil Nadu politics, development news, Chennai updates, and significant events in Tamil Nadu", "News", "Tamil Nadu,Chennai,Tamil politics,TN news,Tamil Nadu development", "1", "5"},
                {"Cricket News", "Latest cricket news, match updates, and player news", "Generate content about cricket matches, player performances, IPL updates, international cricket, and cricket controversies", "Sports", "cricket,IPL,BCCI,Indian cricket,international cricket,cricket news", "1", "5"},
                {"Bollywood News", "Latest Bollywood news, movie releases, and celebrity updates", "Generate content about Bollywood movies, celebrity news, film releases, box office updates, and entertainment industry news", "Entertainment", "Bollywood,Hindi movies,Bollywood news,celebrity news,film industry", "1", "5"},
                {"South Indian Movies", "Latest news from South Indian cinema including Tamil, Telugu, Malayalam, and Kannada films", "Generate content about South Indian movies, regional cinema, Kollywood, Tollywood, Mollywood, and Sandalwood news", "Entertainment", "South Indian movies,Kollywood,Tollywood,Mollywood,Sandalwood,regional cinema", "1", "5"}
            };
            
            int created = 0;
            for (String[] topicData : indiaTopics) {
                String name = topicData[0];
                String description = topicData[1];
                String aiPrompt = topicData[2];
                String category = topicData[3];
                String keywords = topicData[4];
                Integer priority = Integer.parseInt(topicData[5]);
                Integer threadsPerRun = Integer.parseInt(topicData[6]);
                
                // Check if topic already exists
                if (!topicService.getTopicByName(name).isPresent()) {
                    topicService.createTopic(name, description, aiPrompt, category, keywords, priority, threadsPerRun);
                    created++;
                }
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Created " + created + " new India-focused topics");
            response.put("status", "success");
            response.put("created", String.valueOf(created));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error creating India topics: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/set-ai-password")
    public ResponseEntity<Map<String, String>> setAiAssistantPassword(@RequestBody Map<String, String> request) {
        try {
            String password = request.get("password");
            if (password == null || password.trim().isEmpty()) {
                password = "ai_assistant_2024"; // Default password
            }
            
            aiAdminService.setAiAssistantPassword(password);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "AI Assistant password set successfully");
            response.put("status", "success");
            response.put("email", "ai@urutte.com");
            response.put("password", password);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error setting AI Assistant password: " + e.getMessage());
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
