package com.urutte.dto;

public class QuoteRepostRequest {
    private String content;
    private Long quotedPostId;
    private String mediaUrl;
    private String mediaType;

    // Constructors
    public QuoteRepostRequest() {}

    public QuoteRepostRequest(String content, Long quotedPostId, String mediaUrl, String mediaType) {
        this.content = content;
        this.quotedPostId = quotedPostId;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
    }

    // Getters and setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getQuotedPostId() { return quotedPostId; }
    public void setQuotedPostId(Long quotedPostId) { this.quotedPostId = quotedPostId; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
}
