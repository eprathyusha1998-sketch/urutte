package com.urutte.dto;

import java.time.Instant;

public class PostDto {
    private Long id;
    private String content;
    private Instant timestamp;
    private String userId;
    private String userName;
    private String userEmail;
    private String userPicture;
    private Long likes;
    private Long retweets;
    private Long replies;
    private Long reposts; // Add reposts field
    private Long commentsCount;
    private boolean isLiked;
    private boolean isRetweeted;
    private boolean isReposted; // Add reposted status
    private Long parentPostId; // For replies
    private String mediaUrl;
    private String mediaType;
    
    // Thread hierarchy fields
    private Long rootPostId; // Points to the main thread post
    private Integer threadLevel = 0; // 0 = main post, 1 = first level reply, etc.
    private String threadPath; // e.g., "1.2.3" for hierarchical path
    
    // Quote repost fields
    private Long quotedPostId; // Original post being quoted
    private Boolean isQuoteRepost = false;
    private PostDto quotedPost; // Full quoted post data

    // Constructors
    public PostDto() {}

    public PostDto(Long id, String content, Instant timestamp, String userId, 
                   String userName, String userEmail, String userPicture) {
        this.id = id;
        this.content = content;
        this.timestamp = timestamp;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPicture = userPicture;
        this.likes = 0L;
        this.retweets = 0L;
        this.replies = 0L;
        this.reposts = 0L;
        this.commentsCount = 0L;
        this.isLiked = false;
        this.isRetweeted = false;
        this.isReposted = false;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserPicture() { return userPicture; }
    public void setUserPicture(String userPicture) { this.userPicture = userPicture; }

    public Long getLikes() { return likes; }
    public void setLikes(Long likes) { this.likes = likes; }

    public Long getRetweets() { return retweets; }
    public void setRetweets(Long retweets) { this.retweets = retweets; }

    public Long getReplies() { return replies; }
    public void setReplies(Long replies) { this.replies = replies; }

    public Long getCommentsCount() { return commentsCount; }
    public void setCommentsCount(Long commentsCount) { this.commentsCount = commentsCount; }

    public boolean isLiked() { return isLiked; }
    public void setLiked(boolean liked) { isLiked = liked; }

    public boolean isRetweeted() { return isRetweeted; }
    public void setRetweeted(boolean retweeted) { isRetweeted = retweeted; }

    public Long getReposts() { return reposts; }
    public void setReposts(Long reposts) { this.reposts = reposts; }

    public boolean isReposted() { return isReposted; }
    public void setReposted(boolean reposted) { isReposted = reposted; }

    public Long getParentPostId() { return parentPostId; }
    public void setParentPostId(Long parentPostId) { this.parentPostId = parentPostId; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    
    // Thread hierarchy getters and setters
    public Long getRootPostId() { return rootPostId; }
    public void setRootPostId(Long rootPostId) { this.rootPostId = rootPostId; }
    
    public Integer getThreadLevel() { return threadLevel; }
    public void setThreadLevel(Integer threadLevel) { this.threadLevel = threadLevel; }
    
    public String getThreadPath() { return threadPath; }
    public void setThreadPath(String threadPath) { this.threadPath = threadPath; }
    
    // Quote repost getters and setters
    public Long getQuotedPostId() { return quotedPostId; }
    public void setQuotedPostId(Long quotedPostId) { this.quotedPostId = quotedPostId; }
    
    public Boolean getIsQuoteRepost() { return isQuoteRepost; }
    public void setIsQuoteRepost(Boolean isQuoteRepost) { this.isQuoteRepost = isQuoteRepost; }
    
    public PostDto getQuotedPost() { return quotedPost; }
    public void setQuotedPost(PostDto quotedPost) { this.quotedPost = quotedPost; }
}
