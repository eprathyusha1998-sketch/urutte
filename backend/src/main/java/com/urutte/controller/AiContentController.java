package com.urutte.controller;

import com.urutte.model.AiAdmin;
import com.urutte.model.Topic;
import com.urutte.model.Thread;
import com.urutte.model.User;
import com.urutte.service.*;
import com.urutte.service.ContentGenerationService.NewsItem;
import com.urutte.repository.ThreadRepository;
import com.urutte.repository.UserRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/ai-content")
@CrossOrigin(origins = "*")
public class AiContentController {
    
    private static final Logger logger = LoggerFactory.getLogger(AiContentController.class);
    
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
    
    @Autowired
    private NewsScrapingService newsScrapingService;
    
    @Autowired
    private ThreadSummaryService threadSummaryService;
    
    @Autowired
    private ThreadRepository threadRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ThreadSummaryScheduler threadSummaryScheduler;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
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
            response.put("email", "ai.assistant@urutte.com");
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
        public void setThreadsPerRun(Integer threadsPerRun) { this.threadsPerRun = threadsPerRun;         }
    }
    
    /**
     * Test news scraping for a specific category
     */
    @GetMapping("/test-scraping/{category}")
    public ResponseEntity<Map<String, Object>> testNewsScraping(@PathVariable String category) {
        try {
            List<NewsItem> newsItems = newsScrapingService.getNewsByCategory(category);
            
            Map<String, Object> response = new HashMap<>();
            response.put("category", category);
            response.put("count", newsItems.size());
            response.put("newsItems", newsItems.stream().limit(5).map(item -> {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("title", item.getTitle());
                itemMap.put("content", item.getContent().length() > 200 ? 
                    item.getContent().substring(0, 200) + "..." : item.getContent());
                itemMap.put("url", item.getUrl());
                itemMap.put("score", item.getScore());
                itemMap.put("source", item.getSource());
                return itemMap;
            }).toList());
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error testing news scraping: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Test news scraping for a specific topic
     */
    @GetMapping("/test-scraping/topic/{topicId}")
    public ResponseEntity<Map<String, Object>> testNewsScrapingForTopic(@PathVariable Long topicId) {
        try {
            Optional<Topic> topicOpt = topicService.getTopicById(topicId.toString());
            if (topicOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Topic not found");
                response.put("status", "error");
                return ResponseEntity.notFound().build();
            }
            
            Topic topic = topicOpt.get();
            List<NewsItem> newsItems = newsScrapingService.scrapeNewsForTopic(topic);
            
            Map<String, Object> response = new HashMap<>();
            response.put("topicId", topicId);
            response.put("topicName", topic.getName());
            response.put("count", newsItems.size());
            response.put("newsItems", newsItems.stream().limit(5).map(item -> {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("title", item.getTitle());
                itemMap.put("content", item.getContent().length() > 200 ? 
                    item.getContent().substring(0, 200) + "..." : item.getContent());
                itemMap.put("url", item.getUrl());
                itemMap.put("score", item.getScore());
                itemMap.put("source", item.getSource());
                return itemMap;
            }).toList());
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error testing news scraping for topic: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/generate-summary-thread/{topicId}")
    public ResponseEntity<Map<String, Object>> generateSummaryThread(@PathVariable Long topicId) {
        try {
            Optional<Topic> topicOpt = topicService.getTopicById(topicId.toString());
            if (topicOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Topic not found");
                response.put("status", "error");
                return ResponseEntity.notFound().build();
            }
            
            Topic topic = topicOpt.get();
            List<Thread> summaryThreads = threadSummaryService.generateSummaryThread(topic, 3);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Generated " + summaryThreads.size() + " summary posts for topic: " + topic.getName());
            response.put("status", "success");
            response.put("topicId", topicId);
            response.put("topicName", topic.getName());
            response.put("threadsGenerated", summaryThreads.size());
            response.put("threads", summaryThreads.stream().map(thread -> {
                Map<String, Object> threadMap = new HashMap<>();
                threadMap.put("id", thread.getId());
                threadMap.put("content", thread.getContent());
                threadMap.put("createdAt", thread.getCreatedAt());
                return threadMap;
            }).toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error generating summary thread: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/generate-summary-thread-by-name/{topicName}")
    public ResponseEntity<Map<String, Object>> generateSummaryThreadByName(@PathVariable String topicName) {
        try {
            Optional<Topic> topicOpt = topicService.getTopicByName(topicName);
            if (topicOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Topic not found: " + topicName);
                response.put("status", "error");
                return ResponseEntity.notFound().build();
            }
            
            Topic topic = topicOpt.get();
            List<Thread> summaryThreads = threadSummaryService.generateSummaryThread(topic, 3);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Generated " + summaryThreads.size() + " summary posts for topic: " + topic.getName());
            response.put("status", "success");
            response.put("topicId", topic.getId());
            response.put("topicName", topic.getName());
            response.put("threadsGenerated", summaryThreads.size());
            response.put("threads", summaryThreads.stream().map(thread -> {
                Map<String, Object> threadMap = new HashMap<>();
                threadMap.put("id", thread.getId());
                threadMap.put("content", thread.getContent());
                threadMap.put("createdAt", thread.getCreatedAt());
                return threadMap;
            }).toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error generating summary thread: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/debug-summary/{topicName}")
    public ResponseEntity<Map<String, Object>> debugSummaryGeneration(@PathVariable String topicName) {
        try {
            Optional<Topic> topicOpt = topicService.getTopicByName(topicName);
            if (topicOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Topic not found: " + topicName);
                response.put("status", "error");
                return ResponseEntity.notFound().build();
            }
            
            Topic topic = topicOpt.get();
            
            // Test Guardian API directly
            String apiKey = "cca24b40-7d6e-44c8-ba61-1b9039ff961f";
            String baseUrl = "https://content.guardianapis.com";
            String searchQuery = topic.getName();
            
            String url = baseUrl + "/search";
            String fullUrl = url + "?api-key=" + apiKey + 
                           "&q=" + searchQuery.replace(" ", "%20") +
                           "&page-size=3" +
                           "&show-fields=headline,trailText" +
                           "&order-by=newest";
            
            Map<String, Object> response = new HashMap<>();
            response.put("topicName", topic.getName());
            response.put("topicKeywords", topic.getKeywords());
            response.put("guardianApiUrl", fullUrl);
            response.put("status", "debug");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error debugging summary generation: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/test-direct-summary/{topicName}")
    public ResponseEntity<Map<String, Object>> testDirectSummary(@PathVariable String topicName) {
        try {
            Optional<Topic> topicOpt = topicService.getTopicByName(topicName);
            if (topicOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Topic not found: " + topicName);
                response.put("status", "error");
                return ResponseEntity.notFound().build();
            }
            
            Topic topic = topicOpt.get();
            
            // Get AI Assistant user
            Optional<User> aiUserOpt = userRepository.findByEmail("ai.assistant@urutte.com");
            if (aiUserOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "AI Assistant user not found");
                response.put("status", "error");
                return ResponseEntity.notFound().build();
            }
            
            User aiUser = aiUserOpt.get();
            
            // Create a simple test thread
            Thread testThread = new Thread();
            testThread.setContent("📰 Test summary post for " + topic.getName() + "\n\nThis is a test post to verify the system is working.\n\nSource: https://www.theguardian.com\n\n#Test #" + topic.getName().replace(" ", "") + " #News");
            testThread.setUser(aiUser);
            testThread.setCreatedAt(LocalDateTime.now());
            testThread.setUpdatedAt(LocalDateTime.now());
            
            Thread savedThread = threadRepository.save(testThread);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Created test thread for topic: " + topic.getName());
            response.put("status", "success");
            response.put("threadId", savedThread.getId());
            response.put("threadContent", savedThread.getContent());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error creating test thread: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/scheduler/generate-now")
    public ResponseEntity<Map<String, Object>> generateThreadSummariesNow() {
        try {
            threadSummaryScheduler.generateThreadSummariesNow();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Thread summary generation started for all active topics");
            response.put("status", "success");
            response.put("schedule", "Every 2 hours");
            response.put("rateLimit", "60 requests/day (within 500 developer limit)");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error starting thread summary generation: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/scheduler/generate-topic/{topicName}")
    public ResponseEntity<Map<String, Object>> generateSummariesForTopic(@PathVariable String topicName) {
        try {
            threadSummaryScheduler.generateSummariesForTopicNow(topicName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Thread summary generation started for topic: " + topicName);
            response.put("status", "success");
            response.put("topicName", topicName);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error generating summaries for topic: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/scheduler/status")
    public ResponseEntity<Map<String, Object>> getSchedulerStatus() {
        try {
            List<Topic> activeTopics = topicService.getAllActiveTopics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "active");
            response.put("schedule", "Every 2 hours");
            response.put("activeTopics", activeTopics.size());
            response.put("rateLimit", "60 requests/day (within 500 developer limit)");
            response.put("topics", activeTopics.stream().map(topic -> {
                Map<String, Object> topicMap = new HashMap<>();
                topicMap.put("name", topic.getName());
                topicMap.put("category", topic.getCategory());
                topicMap.put("keywords", topic.getKeywords());
                topicMap.put("lastGenerated", topic.getLastGeneratedAt());
                return topicMap;
            }).toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error getting scheduler status: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/demo/insert-sample-threads")
    public ResponseEntity<Map<String, Object>> insertSampleThreads() {
        try {
            // Get AI Assistant user
            Optional<User> aiUserOpt = userRepository.findByEmail("ai.assistant@urutte.com");
            if (aiUserOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "AI Assistant user not found");
                response.put("status", "error");
                return ResponseEntity.notFound().build();
            }
            
            User aiUser = aiUserOpt.get();
            
            // Create sample thread summaries
            String[] sampleThreads = {
                "📰 India to host 2030 Commonwealth Games – next stop the 2036 Olympics?\n\nIndia will be formally approved as hosts of the 2030 Commonwealth Games next month as the country steps up its ambitions to stage the 2036 Olympics. This marks a significant milestone in India's sporting infrastructure development.\n\nSource: https://www.theguardian.com/sport/2025/oct/15/india-to-host-2030-commonwealth-games-next-stop-the-2036-olympics\n\n#India #CommonwealthGames #Olympics #Sports #News",
                
                "🏏 Marnus Labuschagne dropped from Australia ODI squad to face India\n\nMatt Renshaw in line for surprise white-ball international debut while Mitchell Starc returns as 50-over and T20 squads named. Australia prepares for the upcoming series against India with strategic changes.\n\nSource: https://www.theguardian.com/sport/2025/oct/07/cricket-australia-squad-odi-t20-india-marnus-labuschagne-matt-renshaw\n\n#Cricket #India #Australia #Sports #News",
                
                "🌍 Clive of India isn't welcome in Shrewsbury either\n\nLetters: Jean Garner objects to his monument in the Shropshire town and is hoping for Thangam Debbonaire's support in getting it removed. This reflects ongoing discussions about colonial history and monuments.\n\nSource: https://www.theguardian.com/world/2025/aug/18/clive-of-india-isnt-welcome-in-shrewsbury-either\n\n#India #History #Monuments #WorldNews #News"
            };
            
            int created = 0;
            for (String content : sampleThreads) {
                Thread thread = new Thread();
                thread.setContent(content);
                thread.setUser(aiUser);
                thread.setCreatedAt(LocalDateTime.now().minusMinutes(created * 3)); // Stagger timestamps
                thread.setUpdatedAt(LocalDateTime.now().minusMinutes(created * 3));
                
                threadRepository.save(thread);
                created++;
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Created " + created + " sample thread summaries");
            response.put("status", "success");
            response.put("threadsCreated", created);
            response.put("note", "These threads will appear in the feed page");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error creating sample threads: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/demo/create-ai-user")
    public ResponseEntity<Map<String, Object>> createAiUser() {
        try {
            // Check if AI user already exists
            Optional<User> existingUser = userRepository.findByEmail("ai.assistant@urutte.com");
            if (existingUser.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "AI Assistant user already exists");
                response.put("status", "success");
                response.put("userId", existingUser.get().getId());
                return ResponseEntity.ok(response);
            }
            
            // Create AI Assistant user
            User aiUser = new User();
            aiUser.setId("ai-assistant-001");
            aiUser.setEmail("ai.assistant@urutte.com");
            aiUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi"); // password: "password"
            aiUser.setName("AI Assistant");
            aiUser.setUsername("ai_assistant_bot");
            aiUser.setIsActive(true);
            aiUser.setCreatedAt(java.time.Instant.now());
            aiUser.setUpdatedAt(java.time.Instant.now());
            
            User savedUser = userRepository.save(aiUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "AI Assistant user created successfully");
            response.put("status", "success");
            response.put("userId", savedUser.getId());
            response.put("email", savedUser.getEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error creating AI user: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/demo/check-ai-user")
    public ResponseEntity<Map<String, Object>> checkAiUser() {
        try {
            Optional<User> aiUser = userRepository.findByEmail("ai.assistant@urutte.com");
            if (aiUser.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "AI Assistant user found");
                response.put("status", "success");
                response.put("userId", aiUser.get().getId());
                response.put("userName", aiUser.get().getName());
                response.put("userEmail", aiUser.get().getEmail());
                response.put("isActive", aiUser.get().getIsActive());
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "AI Assistant user not found");
                response.put("status", "error");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error checking AI user: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/demo/fix-ai-user")
    public ResponseEntity<Map<String, Object>> fixAiUser() {
        try {
            Optional<User> aiUserOpt = userRepository.findByEmail("ai.assistant@urutte.com");
            if (aiUserOpt.isPresent()) {
                User aiUser = aiUserOpt.get();
                aiUser.setName("AI Assistant");
                aiUser.setEmail("ai.assistant@urutte.com");
                aiUser.setUsername("ai_assistant_bot");
                aiUser.setIsActive(true);
                aiUser.setUpdatedAt(java.time.Instant.now());
                
                User savedUser = userRepository.save(aiUser);
                
                Map<String, Object> response = new HashMap<>();
                response.put("message", "AI Assistant user details updated successfully");
                response.put("status", "success");
                response.put("userId", savedUser.getId());
                response.put("userName", savedUser.getName());
                response.put("userEmail", savedUser.getEmail());
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "AI Assistant user not found");
                response.put("status", "error");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error fixing AI user: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/demo/cleanup-posts")
    public ResponseEntity<Map<String, Object>> cleanupPosts() {
        try {
            // Get AI user first
            Optional<User> aiUserOpt = userRepository.findByEmail("ai.assistant@urutte.com");
            if (aiUserOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "AI user not found");
                response.put("status", "error");
                return ResponseEntity.notFound().build();
            }
            
            User aiUser = aiUserOpt.get();
            
            // Find and delete all threads created by the AI user
            List<Thread> aiThreads = threadRepository.findByUserAndIsDeletedFalseOrderByCreatedAtDesc(aiUser, org.springframework.data.domain.Pageable.unpaged()).getContent();
            int deletedCount = aiThreads.size();
            
            for (Thread thread : aiThreads) {
                threadRepository.delete(thread);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cleaned up " + deletedCount + " old posts");
            response.put("status", "success");
            response.put("deletedCount", deletedCount);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error cleaning up posts: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/demo/cleanup-wrong-user-posts")
    public ResponseEntity<Map<String, Object>> cleanupWrongUserPosts() {
        try {
            // Find the prathyusha e user
            Optional<User> wrongUserOpt = userRepository.findByEmail("eprathyusha1998@gmail.com");
            if (wrongUserOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Wrong user not found");
                response.put("status", "error");
                return ResponseEntity.notFound().build();
            }
            
            User wrongUser = wrongUserOpt.get();
            
            // Find and delete all threads created by the wrong user
            List<Thread> wrongUserThreads = threadRepository.findByUserAndIsDeletedFalseOrderByCreatedAtDesc(wrongUser, org.springframework.data.domain.Pageable.unpaged()).getContent();
            int deletedCount = wrongUserThreads.size();
            
            for (Thread thread : wrongUserThreads) {
                threadRepository.delete(thread);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cleaned up " + deletedCount + " posts from wrong user account");
            response.put("status", "success");
            response.put("deletedCount", deletedCount);
            response.put("wrongUserEmail", wrongUser.getEmail());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error cleaning up wrong user posts: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/demo/check-users")
    public ResponseEntity<Map<String, Object>> checkUsers() {
        try {
            // Check AI user
            Optional<User> aiUserOpt = userRepository.findByEmail("ai.assistant@urutte.com");
            Optional<User> wrongUserOpt = userRepository.findByEmail("eprathyusha1998@gmail.com");
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            
            if (aiUserOpt.isPresent()) {
                User aiUser = aiUserOpt.get();
                response.put("aiUser", Map.of(
                    "id", aiUser.getId(),
                    "name", aiUser.getName(),
                    "email", aiUser.getEmail(),
                    "username", aiUser.getUsername()
                ));
            } else {
                response.put("aiUser", "NOT FOUND");
            }
            
            if (wrongUserOpt.isPresent()) {
                User wrongUser = wrongUserOpt.get();
                response.put("wrongUser", Map.of(
                    "id", wrongUser.getId(),
                    "name", wrongUser.getName(),
                    "email", wrongUser.getEmail(),
                    "username", wrongUser.getUsername()
                ));
            } else {
                response.put("wrongUser", "NOT FOUND");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error checking users: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/demo/fix-ai-password")
    public ResponseEntity<Map<String, Object>> fixAiPassword() {
        try {
            Optional<User> aiUserOpt = userRepository.findByEmail("ai.assistant@urutte.com");
            if (aiUserOpt.isPresent()) {
                User aiUser = aiUserOpt.get();
                // Use password encoder to properly hash "password"
                aiUser.setPassword(passwordEncoder.encode("password"));
                aiUser.setUpdatedAt(java.time.Instant.now());
                
                User savedUser = userRepository.save(aiUser);
                
                Map<String, Object> response = new HashMap<>();
                response.put("message", "AI Assistant password updated successfully");
                response.put("status", "success");
                response.put("userId", savedUser.getId());
                response.put("email", savedUser.getEmail());
                response.put("password", "password");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "AI Assistant user not found");
                response.put("status", "error");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error fixing AI password: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/demo/debug-ai-user")
    public ResponseEntity<Map<String, Object>> debugAiUser() {
        try {
            Optional<User> aiUserOpt = userRepository.findByEmail("ai.assistant@urutte.com");
            if (aiUserOpt.isPresent()) {
                User aiUser = aiUserOpt.get();
                
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("userId", aiUser.getId());
                response.put("email", aiUser.getEmail());
                response.put("username", aiUser.getUsername());
                response.put("name", aiUser.getName());
                response.put("isActive", aiUser.getIsActive());
                response.put("passwordHash", aiUser.getPassword());
                response.put("createdAt", aiUser.getCreatedAt());
                response.put("updatedAt", aiUser.getUpdatedAt());
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "AI Assistant user not found");
                response.put("status", "error");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error debugging AI user: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/demo/generate-fresh-content")
    public ResponseEntity<Map<String, Object>> generateFreshContent() {
        try {
            // Generate content for key topics
            String[] topics = {"India News", "Tamil Nadu News", "Cricket News", "Bollywood News", "South Indian Movies"};
            int totalGenerated = 0;
            
            for (String topicName : topics) {
                try {
                    threadSummaryScheduler.generateSummariesForTopicNow(topicName);
                    totalGenerated += 3; // Each topic generates 3 posts
                    // Add delay between topics
                    java.lang.Thread.sleep(2000);
                } catch (Exception e) {
                    logger.error("Error generating content for topic: {}", topicName, e);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Generated fresh content for " + topics.length + " topics");
            response.put("status", "success");
            response.put("topicsProcessed", topics.length);
            response.put("estimatedPosts", totalGenerated);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error generating fresh content: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
}
