package com.urutte.dto;

import java.time.LocalDateTime;

public class NotificationDto {
    private Long id;
    private String fromUserId;
    private String fromUserName;
    private String fromUserAvatar;
    private String type;
    private String title;
    private String message;
    private String relatedEntityType;
    private Long relatedEntityId;
    private boolean isRead;
    private LocalDateTime createdAt;

    // Constructors
    public NotificationDto() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFromUserId() { return fromUserId; }
    public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }

    public String getFromUserName() { return fromUserName; }
    public void setFromUserName(String fromUserName) { this.fromUserName = fromUserName; }

    public String getFromUserAvatar() { return fromUserAvatar; }
    public void setFromUserAvatar(String fromUserAvatar) { this.fromUserAvatar = fromUserAvatar; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRelatedEntityType() { return relatedEntityType; }
    public void setRelatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; }

    public Long getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
