package com.urutte.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    private String content;
    private String messageType; // text, image, video, file
    private String mediaUrl;
    private boolean isRead;
    private LocalDateTime createdAt;

    // Constructors
    public Message() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
        this.messageType = "text";
    }

    public Message(User sender, User receiver, String content) {
        this();
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
    }

    public Message(User sender, User receiver, String content, String messageType, String mediaUrl) {
        this(sender, receiver, content);
        this.messageType = messageType;
        this.mediaUrl = mediaUrl;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
