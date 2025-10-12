package com.urutte.dto;

import java.time.Instant;
import java.util.List;

public class CommentDto {
    private Long id;
    private String content;
    private Long postId;
    private String userId;
    private String userName;
    private String userPicture;
    private Long parentCommentId;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer likesCount;
    private Integer repliesCount;
    private List<CommentDto> replies;
    private boolean isLiked;
    
    // Constructors
    public CommentDto() {}
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getUserPicture() { return userPicture; }
    public void setUserPicture(String userPicture) { this.userPicture = userPicture; }
    
    public Long getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(Long parentCommentId) { this.parentCommentId = parentCommentId; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    
    public Integer getLikesCount() { return likesCount; }
    public void setLikesCount(Integer likesCount) { this.likesCount = likesCount; }
    
    public Integer getRepliesCount() { return repliesCount; }
    public void setRepliesCount(Integer repliesCount) { this.repliesCount = repliesCount; }
    
    public List<CommentDto> getReplies() { return replies; }
    public void setReplies(List<CommentDto> replies) { this.replies = replies; }
    
    public boolean isLiked() { return isLiked; }
    public void setLiked(boolean liked) { isLiked = liked; }
}
