package com.urutte.dto;

public class CreatePostDto {
    private String content;
    private Long parentPostId; // For replies
    private String mediaUrl;
    private String mediaType;

    // Constructors
    public CreatePostDto() {}

    public CreatePostDto(String content) {
        this.content = content;
    }

    // Getters and Setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getParentPostId() { return parentPostId; }
    public void setParentPostId(Long parentPostId) { this.parentPostId = parentPostId; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
}
