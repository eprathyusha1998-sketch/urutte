package com.urutte.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_generated_threads")
public class AiGeneratedThread {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    private Thread thread;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_admin_id", nullable = false)
    private AiAdmin aiAdmin;
    
    @Column(columnDefinition = "TEXT")
    private String originalContent; // Original content from external sources
    
    @Column(columnDefinition = "TEXT")
    private String sourceUrl; // URL of the source content
    
    @Column(columnDefinition = "TEXT")
    private String sourceTitle; // Title of the source content
    
    @Column(nullable = false)
    private String generationMethod; // "openai", "claude", "manual", etc.
    
    @Column(nullable = false)
    private String status = "active"; // "active", "deleted", "flagged"
    
    @Column(columnDefinition = "TEXT")
    private String metadata; // JSON string with additional metadata
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public AiGeneratedThread() {}
    
    public AiGeneratedThread(Thread thread, Topic topic, AiAdmin aiAdmin, String originalContent, 
                           String sourceUrl, String sourceTitle, String generationMethod) {
        this.thread = thread;
        this.topic = topic;
        this.aiAdmin = aiAdmin;
        this.originalContent = originalContent;
        this.sourceUrl = sourceUrl;
        this.sourceTitle = sourceTitle;
        this.generationMethod = generationMethod;
        this.status = "active";
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Thread getThread() { return thread; }
    public void setThread(Thread thread) { this.thread = thread; }
    
    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }
    
    public AiAdmin getAiAdmin() { return aiAdmin; }
    public void setAiAdmin(AiAdmin aiAdmin) { this.aiAdmin = aiAdmin; }
    
    public String getOriginalContent() { return originalContent; }
    public void setOriginalContent(String originalContent) { this.originalContent = originalContent; }
    
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    
    public String getSourceTitle() { return sourceTitle; }
    public void setSourceTitle(String sourceTitle) { this.sourceTitle = sourceTitle; }
    
    public String getGenerationMethod() { return generationMethod; }
    public void setGenerationMethod(String generationMethod) { this.generationMethod = generationMethod; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
