package com.urutte.service;

import com.urutte.model.AiAdmin;
import com.urutte.repository.AiAdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AiAdminService {
    
    private static final Logger logger = LoggerFactory.getLogger(AiAdminService.class);
    
    @Autowired
    private AiAdminRepository aiAdminRepository;
    
    /**
     * Ensure AI Admin exists, create if not
     */
    public void ensureAiAdminExists() {
        Optional<AiAdmin> existingAdmin = aiAdminRepository.findActiveAiAdmin();
        
        if (existingAdmin.isEmpty()) {
            logger.info("No AI Admin found. Creating default AI Admin.");
            createDefaultAiAdmin();
        } else {
            logger.debug("AI Admin already exists: {}", existingAdmin.get().getName());
        }
    }
    
    /**
     * Create default AI Admin
     */
    public AiAdmin createDefaultAiAdmin() {
        AiAdmin aiAdmin = new AiAdmin(
            "AI Assistant",
            "ai_assistant",
            "ai@urutte.com",
            "🤖 AI-powered content curator bringing you the latest trends and discussions from across the web. Always learning, always sharing!",
            "http://localhost/assets/images/avatars/avatar-1.jpg?v=" + System.currentTimeMillis()
        );
        
        aiAdmin = aiAdminRepository.save(aiAdmin);
        logger.info("Created default AI Admin: {}", aiAdmin.getName());
        
        return aiAdmin;
    }
    
    /**
     * Create custom AI Admin
     */
    public AiAdmin createAiAdmin(String name, String username, String email, String bio, String avatarUrl) {
        // Check if username or email already exists
        if (aiAdminRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        
        if (aiAdminRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
        
        AiAdmin aiAdmin = new AiAdmin(name, username, email, bio, avatarUrl);
        aiAdmin = aiAdminRepository.save(aiAdmin);
        
        logger.info("Created custom AI Admin: {}", aiAdmin.getName());
        return aiAdmin;
    }
    
    /**
     * Get active AI Admin
     */
    public Optional<AiAdmin> getActiveAiAdmin() {
        return aiAdminRepository.findActiveAiAdmin();
    }
    
    /**
     * Get AI Admin by username
     */
    public Optional<AiAdmin> getAiAdminByUsername(String username) {
        return aiAdminRepository.findByUsername(username);
    }
    
    /**
     * Get AI Admin by email
     */
    public Optional<AiAdmin> getAiAdminByEmail(String email) {
        return aiAdminRepository.findByEmail(email);
    }
    
    /**
     * Get all AI Admins
     */
    public List<AiAdmin> getAllAiAdmins() {
        return aiAdminRepository.findAll();
    }
    
    /**
     * Update AI Admin
     */
    public AiAdmin updateAiAdmin(Long id, String name, String bio, String avatarUrl) {
        Optional<AiAdmin> aiAdminOpt = aiAdminRepository.findById(id);
        
        if (aiAdminOpt.isEmpty()) {
            throw new IllegalArgumentException("AI Admin not found with id: " + id);
        }
        
        AiAdmin aiAdmin = aiAdminOpt.get();
        aiAdmin.setName(name);
        aiAdmin.setBio(bio);
        aiAdmin.setAvatarUrl(avatarUrl);
        
        aiAdmin = aiAdminRepository.save(aiAdmin);
        logger.info("Updated AI Admin: {}", aiAdmin.getName());
        
        return aiAdmin;
    }
    
    /**
     * Activate/Deactivate AI Admin
     */
    public AiAdmin toggleAiAdminStatus(Long id) {
        Optional<AiAdmin> aiAdminOpt = aiAdminRepository.findById(id);
        
        if (aiAdminOpt.isEmpty()) {
            throw new IllegalArgumentException("AI Admin not found with id: " + id);
        }
        
        AiAdmin aiAdmin = aiAdminOpt.get();
        aiAdmin.setIsActive(!aiAdmin.getIsActive());
        
        aiAdmin = aiAdminRepository.save(aiAdmin);
        logger.info("Toggled AI Admin status: {} (active: {})", aiAdmin.getName(), aiAdmin.getIsActive());
        
        return aiAdmin;
    }
    
    /**
     * Delete AI Admin
     */
    public void deleteAiAdmin(Long id) {
        Optional<AiAdmin> aiAdminOpt = aiAdminRepository.findById(id);
        
        if (aiAdminOpt.isEmpty()) {
            throw new IllegalArgumentException("AI Admin not found with id: " + id);
        }
        
        AiAdmin aiAdmin = aiAdminOpt.get();
        aiAdminRepository.delete(aiAdmin);
        
        logger.info("Deleted AI Admin: {}", aiAdmin.getName());
    }
    
    /**
     * Get AI Admin statistics
     */
    public AiAdminStats getAiAdminStats(Long id) {
        Optional<AiAdmin> aiAdminOpt = aiAdminRepository.findById(id);
        
        if (aiAdminOpt.isEmpty()) {
            throw new IllegalArgumentException("AI Admin not found with id: " + id);
        }
        
        AiAdmin aiAdmin = aiAdminOpt.get();
        
        AiAdminStats stats = new AiAdminStats();
        stats.setId(aiAdmin.getId());
        stats.setName(aiAdmin.getName());
        stats.setUsername(aiAdmin.getUsername());
        stats.setPostsCount(aiAdmin.getPostsCount());
        stats.setFollowersCount(aiAdmin.getFollowersCount());
        stats.setFollowingCount(aiAdmin.getFollowingCount());
        stats.setIsActive(aiAdmin.getIsActive());
        stats.setCreatedAt(aiAdmin.getCreatedAt());
        stats.setLastUpdatedAt(aiAdmin.getUpdatedAt());
        
        return stats;
    }
    
    /**
     * AI Admin statistics data class
     */
    public static class AiAdminStats {
        private Long id;
        private String name;
        private String username;
        private Integer postsCount;
        private Integer followersCount;
        private Integer followingCount;
        private Boolean isActive;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime lastUpdatedAt;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public Integer getPostsCount() { return postsCount; }
        public void setPostsCount(Integer postsCount) { this.postsCount = postsCount; }
        
        public Integer getFollowersCount() { return followersCount; }
        public void setFollowersCount(Integer followersCount) { this.followersCount = followersCount; }
        
        public Integer getFollowingCount() { return followingCount; }
        public void setFollowingCount(Integer followingCount) { this.followingCount = followingCount; }
        
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public java.time.LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
        public void setLastUpdatedAt(java.time.LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
    }
}
