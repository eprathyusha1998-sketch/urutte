package com.urutte.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "follow_requests")
public class FollowRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "target_id", nullable = false)
    private User target;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FollowRequestStatus status = FollowRequestStatus.PENDING;
    
    @Column(nullable = false)
    private Instant createdAt;
    
    private Instant updatedAt;

    // Constructors
    public FollowRequest() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public FollowRequest(User requester, User target) {
        this();
        this.requester = requester;
        this.target = target;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getRequester() { return requester; }
    public void setRequester(User requester) { this.requester = requester; }

    public User getTarget() { return target; }
    public void setTarget(User target) { this.target = target; }

    public FollowRequestStatus getStatus() { return status; }
    public void setStatus(FollowRequestStatus status) { 
        this.status = status; 
        this.updatedAt = Instant.now();
    }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
    
    public enum FollowRequestStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}
