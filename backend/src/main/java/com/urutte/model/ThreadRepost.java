package com.urutte.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "thread_reposts", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"thread_id", "user_id"}))
public class ThreadRepost {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    private Thread thread;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "repost_type", nullable = false)
    private RepostType repostType = RepostType.REPOST;
    
    @Column(name = "quote_content", columnDefinition = "TEXT")
    private String quoteContent;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public ThreadRepost() {}
    
    public ThreadRepost(Thread thread, User user, RepostType repostType) {
        this.thread = thread;
        this.user = user;
        this.repostType = repostType;
    }
    
    public ThreadRepost(Thread thread, User user, RepostType repostType, String quoteContent) {
        this.thread = thread;
        this.user = user;
        this.repostType = repostType;
        this.quoteContent = quoteContent;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Thread getThread() { return thread; }
    public void setThread(Thread thread) { this.thread = thread; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public RepostType getRepostType() { return repostType; }
    public void setRepostType(RepostType repostType) { this.repostType = repostType; }
    
    public String getQuoteContent() { return quoteContent; }
    public void setQuoteContent(String quoteContent) { this.quoteContent = quoteContent; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
