package com.urutte.service;

import com.urutte.model.Topic;
import com.urutte.model.User;
import com.urutte.model.UserTopic;
import com.urutte.repository.TopicRepository;
import com.urutte.repository.UserTopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TopicService {
    
    @Autowired
    private TopicRepository topicRepository;
    
    @Autowired
    private UserTopicRepository userTopicRepository;
    
    private static final int MAX_USER_TOPICS = 5;
    
    /**
     * Get all available topics for a user (excluding already liked ones)
     */
    public List<Topic> getAvailableTopicsForUser(String userId) {
        return topicRepository.findAvailableTopicsForUser(userId);
    }
    
    /**
     * Get random topics for suggestion (excluding already liked ones)
     */
    public List<Topic> getRandomTopicsForUser(String userId, int limit) {
        List<Topic> availableTopics = getAvailableTopicsForUser(userId);
        Collections.shuffle(availableTopics);
        return availableTopics.stream().limit(limit).collect(Collectors.toList());
    }
    
    /**
     * Get user's liked topics
     */
    public List<Topic> getUserLikedTopics(String userId) {
        return userTopicRepository.findLikedTopicsByUserId(userId);
    }
    
    /**
     * Like a topic for a user (maintain max 5 topics)
     */
    public boolean likeTopic(String userId, String topicId) {
        // Check if user already likes this topic
        Optional<UserTopic> existingUserTopic = userTopicRepository.findByUserIdAndTopicId(userId, topicId);
        if (existingUserTopic.isPresent()) {
            return false; // Already liked
        }
        
        // Check if topic exists and is active
        Optional<Topic> topic = topicRepository.findById(topicId);
        if (!topic.isPresent() || !topic.get().getIsActive()) {
            return false; // Topic not found or inactive
        }
        
        // Check if user has reached max topics limit
        long currentTopicCount = userTopicRepository.countByUserId(userId);
        if (currentTopicCount >= MAX_USER_TOPICS) {
            // Remove the oldest topic
            List<UserTopic> userTopics = userTopicRepository.findByUserIdOrderByCreatedAtAsc(userId);
            if (!userTopics.isEmpty()) {
                UserTopic oldestTopic = userTopics.get(0);
                userTopicRepository.delete(oldestTopic);
            }
        }
        
        // Add new topic
        User user = new User();
        user.setId(userId);
        UserTopic userTopic = new UserTopic(user, topic.get());
        userTopicRepository.save(userTopic);
        
        return true;
    }
    
    /**
     * Unlike a topic for a user
     */
    public boolean unlikeTopic(String userId, String topicId) {
        Optional<UserTopic> userTopic = userTopicRepository.findByUserIdAndTopicId(userId, topicId);
        if (userTopic.isPresent()) {
            userTopicRepository.delete(userTopic.get());
            return true;
        }
        return false;
    }
    
    /**
     * Check if user likes a topic
     */
    public boolean isTopicLikedByUser(String userId, String topicId) {
        return userTopicRepository.findByUserIdAndTopicId(userId, topicId).isPresent();
    }
    
    /**
     * Get all active topics (for admin purposes)
     */
    public List<Topic> getAllActiveTopics() {
        return topicRepository.findByIsActiveTrue();
    }
    
    /**
     * Get all categories
     */
    public List<String> getAllCategories() {
        return topicRepository.findDistinctCategories();
    }
    
    /**
     * Get topics by category
     */
    public List<Topic> getTopicsByCategory(String category) {
        return topicRepository.findByCategory(category);
    }
    
    /**
     * Search topics
     */
    public List<Topic> searchTopics(String query) {
        return topicRepository.searchTopics(query);
    }
    
    /**
     * Create a new topic
     */
    public Topic createTopic(String name, String description, String aiPrompt, String category, String keywords) {
        Topic topic = new Topic(name, description, aiPrompt, category, keywords);
        return topicRepository.save(topic);
    }
    
    /**
     * Create a new topic with all parameters
     */
    public Topic createTopic(String name, String description, String aiPrompt, String category, String keywords, Integer priority, Integer threadsPerRun) {
        Topic topic = new Topic(name, description, aiPrompt, category, keywords);
        topic.setPriority(priority);
        topic.setThreadsPerRun(threadsPerRun);
        return topicRepository.save(topic);
    }
    
    /**
     * Update a topic
     */
    public Topic updateTopic(String id, String name, String description, String aiPrompt, String category, String keywords, Integer priority, Integer threadsPerRun) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));
        
        topic.setName(name);
        topic.setDescription(description);
        topic.setAiPrompt(aiPrompt);
        topic.setCategory(category);
        topic.setKeywords(keywords);
        topic.setPriority(priority);
        topic.setThreadsPerRun(threadsPerRun);
        
        return topicRepository.save(topic);
    }
    
    /**
     * Toggle topic status
     */
    public Topic toggleTopicStatus(String id) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));
        
        topic.setIsActive(!topic.getIsActive());
        return topicRepository.save(topic);
    }
    
    /**
     * Get topic by ID
     */
    public Optional<Topic> getTopicById(String id) {
        return topicRepository.findById(id);
    }
    
    /**
     * Get topic by name
     */
    public Optional<Topic> getTopicByName(String name) {
        return topicRepository.findByName(name);
    }
    
    /**
     * Get topic stats
     */
    public TopicStats getTopicStats(String id) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));
        
        return new TopicStats(
            Long.parseLong(topic.getId()),
            topic.getName(),
            topic.getThreadsGenerated(),
            topic.getLastGeneratedAt(),
            topic.getIsActive()
        );
    }
    
    /**
     * TopicStats inner class
     */
    public static class TopicStats {
        private Long id;
        private String name;
        private Integer threadsGenerated;
        private java.time.LocalDateTime lastGeneratedAt;
        private Boolean isActive;
        
        public TopicStats(Long id, String name, Integer threadsGenerated, java.time.LocalDateTime lastGeneratedAt, Boolean isActive) {
            this.id = id;
            this.name = name;
            this.threadsGenerated = threadsGenerated;
            this.lastGeneratedAt = lastGeneratedAt;
            this.isActive = isActive;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getName() { return name; }
        public Integer getThreadsGenerated() { return threadsGenerated; }
        public java.time.LocalDateTime getLastGeneratedAt() { return lastGeneratedAt; }
        public Boolean getIsActive() { return isActive; }
    }
    
    /**
     * Initialize default topics if they don't exist
     */
    public void initializeDefaultTopics() {
        String[][] defaultTopics = {
            {"Stock Market", "Latest updates on AMD, NVIDIA, Apple, Meta, SOFI, NBIS, HIMS and top stock market news including Fed meetings", "Generate content about stock market trends, earnings reports, and financial news focusing on AMD, NVIDIA, Apple, Meta, SOFI, NBIS, HIMS and Federal Reserve meetings", "Finance", "AMD,NVIDIA,Apple,Meta,SOFI,NBIS,HIMS,stock market,earnings,Fed meetings"},
            {"Sports", "Cricket, football, basketball and other sports news and updates", "Generate content about sports news, match results, player updates, and analysis covering cricket, football, basketball and other major sports", "Sports", "cricket,football,basketball,sports news,match results,player updates"},
            {"US Top News", "Latest breaking news and important updates from the United States", "Generate content about top US news, political developments, social issues, and major events happening in the United States", "News", "US news,politics,social issues,breaking news"},
            {"Tamil Nadu India News", "Latest news and updates from Tamil Nadu, India", "Generate content about Tamil Nadu politics, development, culture, and important news from the state", "Regional", "Tamil Nadu,politics,development,culture,India"},
            {"India Top News", "Latest national news and updates from India", "Generate content about Indian politics, economy, social issues, and major national developments", "News", "India,politics,economy,social issues,national news"},
            {"World Cinema", "Latest updates from global cinema, movies, and entertainment industry", "Generate content about new movie releases, film industry news, celebrity updates, and entertainment trends worldwide", "Entertainment", "movies,cinema,entertainment,celebrity,film industry"},
            {"White House", "Updates on Trump, Senate, Congress and US political developments", "Generate content about US political developments, White House news, Trump updates, Senate proceedings, and Congressional activities", "Politics", "Trump,Senate,Congress,White House,US politics"},
            {"AI Innovation", "Latest developments in artificial intelligence and technology innovation", "Generate content about AI breakthroughs, tech innovations, machine learning developments, and emerging technologies", "Technology", "AI,artificial intelligence,technology,innovation,machine learning"},
            {"TVK Tamil Politics", "Updates on Tamil politics and TVK party developments", "Generate content about Tamil political developments, TVK party news, and Tamil political landscape", "Politics", "TVK,Tamil politics,political developments"},
            {"Top US News", "Breaking news and important updates from across the United States", "Generate content about major US news stories, political developments, and significant events across the country", "News", "US news,breaking news,political developments"},
            {"India News", "Latest news and updates from across India", "Generate content about major Indian news stories, political developments, economic updates, and significant events across India", "News", "India news,Indian politics,Indian economy,breaking news India"},
            {"Tamil Nadu News", "Latest news and updates from Tamil Nadu state", "Generate content about Tamil Nadu politics, development news, Chennai updates, and significant events in Tamil Nadu", "News", "Tamil Nadu,Chennai,Tamil politics,TN news,Tamil Nadu development"},
            {"Cricket News", "Latest cricket news, match updates, and player news", "Generate content about cricket matches, player performances, IPL updates, international cricket, and cricket controversies", "Sports", "cricket,IPL,BCCI,Indian cricket,international cricket,cricket news"},
            {"Bollywood News", "Latest Bollywood news, movie releases, and celebrity updates", "Generate content about Bollywood movies, celebrity news, film releases, box office updates, and entertainment industry news", "Entertainment", "Bollywood,Hindi movies,Bollywood news,celebrity news,film industry"},
            {"South Indian Movies", "Latest news from South Indian cinema including Tamil, Telugu, Malayalam, and Kannada films", "Generate content about South Indian movies, regional cinema, Kollywood, Tollywood, Mollywood, and Sandalwood news", "Entertainment", "South Indian movies,Kollywood,Tollywood,Mollywood,Sandalwood,regional cinema"}
        };
        
        for (String[] topicData : defaultTopics) {
            String name = topicData[0];
            String description = topicData[1];
            String aiPrompt = topicData[2];
            String category = topicData[3];
            String keywords = topicData[4];
            
            Optional<Topic> existingTopic = topicRepository.findByName(name);
            if (!existingTopic.isPresent()) {
                Topic topic = new Topic(name, description, aiPrompt, category, keywords);
                topicRepository.save(topic);
            }
        }
    }
}