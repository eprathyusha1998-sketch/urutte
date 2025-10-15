package com.urutte.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;

@Entity
@Table(name = "topics")
public class Topic {
    
    @Id
    @Column(name = "id")
    private String id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "ai_prompt", columnDefinition = "TEXT")
    private String aiPrompt;
    
    @Column(name = "category")
    private String category;
    
    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;
    
    @Column(name = "priority")
    private Integer priority = 5;
    
    @Column(name = "threads_per_run")
    private Integer threadsPerRun = 3;
    
    @Column(name = "threads_generated")
    private Integer threadsGenerated = 0;
    
    @Column(name = "last_generated_at")
    private LocalDateTime lastGeneratedAt;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private Instant createdAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    // Constructors
    public Topic() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    public Topic(String name, String description, String aiPrompt) {
        this();
        this.name = name;
        this.description = description;
        this.aiPrompt = aiPrompt;
    }
    
    public Topic(String name, String description, String aiPrompt, String category, String keywords) {
        this();
        this.name = name;
        this.description = description;
        this.aiPrompt = aiPrompt;
        this.category = category;
        this.keywords = keywords;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getAiPrompt() {
        return aiPrompt;
    }
    
    public void setAiPrompt(String aiPrompt) {
        this.aiPrompt = aiPrompt;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getKeywords() {
        return keywords;
    }
    
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public Integer getThreadsPerRun() {
        return threadsPerRun;
    }
    
    public void setThreadsPerRun(Integer threadsPerRun) {
        this.threadsPerRun = threadsPerRun;
    }
    
    public Integer getThreadsGenerated() {
        return threadsGenerated;
    }
    
    public void setThreadsGenerated(Integer threadsGenerated) {
        this.threadsGenerated = threadsGenerated;
    }
    
    public LocalDateTime getLastGeneratedAt() {
        return lastGeneratedAt;
    }
    
    public void setLastGeneratedAt(LocalDateTime lastGeneratedAt) {
        this.lastGeneratedAt = lastGeneratedAt;
    }
    
    // Helper methods
    public String[] getKeywordsArray() {
        if (keywords == null || keywords.trim().isEmpty()) {
            return new String[0];
        }
        return Arrays.stream(keywords.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }
    
    public void updateLastGeneratedAt() {
        this.lastGeneratedAt = LocalDateTime.now();
    }
    
    public void incrementThreadsGenerated() {
        this.threadsGenerated = (this.threadsGenerated == null ? 0 : this.threadsGenerated) + 1;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}