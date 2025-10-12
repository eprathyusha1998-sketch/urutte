package com.urutte.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "thread_mentions")
public class ThreadMention {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    private Thread thread;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentioned_user_id", nullable = false)
    private User mentionedUser;
    
    @Column(name = "mention_start", nullable = false)
    private Integer mentionStart; // Position in content where mention starts
    
    @Column(name = "mention_end", nullable = false)
    private Integer mentionEnd;   // Position in content where mention ends
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public ThreadMention() {}
    
    public ThreadMention(Thread thread, User mentionedUser, Integer mentionStart, Integer mentionEnd) {
        this.thread = thread;
        this.mentionedUser = mentionedUser;
        this.mentionStart = mentionStart;
        this.mentionEnd = mentionEnd;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Thread getThread() { return thread; }
    public void setThread(Thread thread) { this.thread = thread; }
    
    public User getMentionedUser() { return mentionedUser; }
    public void setMentionedUser(User mentionedUser) { this.mentionedUser = mentionedUser; }
    
    public Integer getMentionStart() { return mentionStart; }
    public void setMentionStart(Integer mentionStart) { this.mentionStart = mentionStart; }
    
    public Integer getMentionEnd() { return mentionEnd; }
    public void setMentionEnd(Integer mentionEnd) { this.mentionEnd = mentionEnd; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
