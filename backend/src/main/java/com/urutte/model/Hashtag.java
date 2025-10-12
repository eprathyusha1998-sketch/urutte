package com.urutte.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hashtags")
public class Hashtag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tag", unique = true, nullable = false, length = 100)
    private String tag;
    
    @Column(name = "usage_count")
    private Integer usageCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "hashtag", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ThreadHashtag> threadHashtags = new ArrayList<>();
    
    // Constructors
    public Hashtag() {}
    
    public Hashtag(String tag) {
        this.tag = tag;
        this.usageCount = 0;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    
    public Integer getUsageCount() { return usageCount; }
    public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<ThreadHashtag> getThreadHashtags() { return threadHashtags; }
    public void setThreadHashtags(List<ThreadHashtag> threadHashtags) { this.threadHashtags = threadHashtags; }
    
    // Helper methods
    public void incrementUsageCount() {
        this.usageCount = (this.usageCount == null ? 0 : this.usageCount) + 1;
    }
    
    public void decrementUsageCount() {
        this.usageCount = Math.max(0, (this.usageCount == null ? 0 : this.usageCount) - 1);
    }
}
