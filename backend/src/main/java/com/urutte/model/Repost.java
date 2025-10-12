package com.urutte.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "reposts")
public class Repost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_post_id")
    private Post originalPost;
    
    private Instant timestamp;

    // Constructors
    public Repost() {
        this.timestamp = Instant.now();
    }

    public Repost(User user, Post originalPost) {
        this();
        this.user = user;
        this.originalPost = originalPost;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Post getOriginalPost() { return originalPost; }
    public void setOriginalPost(Post originalPost) { this.originalPost = originalPost; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
