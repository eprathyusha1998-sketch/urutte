package com.urutte.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String content;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment; // For replies
    
    @Column(nullable = false)
    private Instant createdAt;
    
    @Column(nullable = false)
    private Instant updatedAt;
    
    // Counters (denormalized for performance)
    @Column(nullable = false)
    private Integer likesCount = 0;
    
    @Column(nullable = false)
    private Integer repliesCount = 0;
    
    // Constructors
    public Comment() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    public Comment(String content, Post post, User user) {
        this();
        this.content = content;
        this.post = post;
        this.user = user;
    }
    
    public Comment(String content, Post post, User user, Comment parentComment) {
        this(content, post, user);
        this.parentComment = parentComment;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Comment getParentComment() { return parentComment; }
    public void setParentComment(Comment parentComment) { this.parentComment = parentComment; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    
    public Integer getLikesCount() { return likesCount; }
    public void setLikesCount(Integer likesCount) { this.likesCount = likesCount; }
    
    public Integer getRepliesCount() { return repliesCount; }
    public void setRepliesCount(Integer repliesCount) { this.repliesCount = repliesCount; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
