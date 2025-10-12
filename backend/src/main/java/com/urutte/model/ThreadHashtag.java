package com.urutte.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "thread_hashtags", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"thread_id", "hashtag_id"}))
public class ThreadHashtag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    private Thread thread;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtag_id", nullable = false)
    private Hashtag hashtag;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public ThreadHashtag() {}
    
    public ThreadHashtag(Thread thread, Hashtag hashtag) {
        this.thread = thread;
        this.hashtag = hashtag;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Thread getThread() { return thread; }
    public void setThread(Thread thread) { this.thread = thread; }
    
    public Hashtag getHashtag() { return hashtag; }
    public void setHashtag(Hashtag hashtag) { this.hashtag = hashtag; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
