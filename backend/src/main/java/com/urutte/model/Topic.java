package com.urutte.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "topics")
public class Topic {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private String category;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private Integer priority = 1; // 1-10, higher number = higher priority
    
    @Column(nullable = false)
    private Integer threadsPerRun = 3; // Number of threads to generate per run
    
    @Column(columnDefinition = "TEXT")
    private String keywords; // Comma-separated keywords for content generation
    
    @Column(columnDefinition = "TEXT")
    private String searchQueries; // Comma-separated search queries
    
    @Column(nullable = false)
    private LocalDateTime lastGeneratedAt;
    
    @Column(nullable = false)
    private Integer totalThreadsGenerated = 0;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public Topic() {}
    
    public Topic(String name, String description, String category, String keywords, String searchQueries) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.keywords = keywords;
        this.searchQueries = searchQueries;
        this.isActive = true;
        this.priority = 1;
        this.threadsPerRun = 3;
        this.totalThreadsGenerated = 0;
        this.lastGeneratedAt = LocalDateTime.now().minusHours(24); // Start with old timestamp
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    
    public Integer getThreadsPerRun() { return threadsPerRun; }
    public void setThreadsPerRun(Integer threadsPerRun) { this.threadsPerRun = threadsPerRun; }
    
    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    
    public String getSearchQueries() { return searchQueries; }
    public void setSearchQueries(String searchQueries) { this.searchQueries = searchQueries; }
    
    public LocalDateTime getLastGeneratedAt() { return lastGeneratedAt; }
    public void setLastGeneratedAt(LocalDateTime lastGeneratedAt) { this.lastGeneratedAt = lastGeneratedAt; }
    
    public Integer getTotalThreadsGenerated() { return totalThreadsGenerated; }
    public void setTotalThreadsGenerated(Integer totalThreadsGenerated) { this.totalThreadsGenerated = totalThreadsGenerated; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods
    public void incrementThreadsGenerated() {
        this.totalThreadsGenerated = (this.totalThreadsGenerated == null ? 0 : this.totalThreadsGenerated) + 1;
    }
    
    public void updateLastGeneratedAt() {
        this.lastGeneratedAt = LocalDateTime.now();
    }
    
    public String[] getKeywordsArray() {
        if (keywords == null || keywords.trim().isEmpty()) {
            return new String[0];
        }
        return keywords.split(",");
    }
    
    public String[] getSearchQueriesArray() {
        if (searchQueries == null || searchQueries.trim().isEmpty()) {
            return new String[0];
        }
        return searchQueries.split(",");
    }
}
