package com.urutte.dto;

import com.urutte.model.ThreadType;
import com.urutte.model.MediaType;
import com.urutte.model.ReactionType;

import java.time.LocalDateTime;
import java.util.List;

public class ThreadDto {
    private Long id;
    private String content;
    private ThreadType threadType;
    
    // User information
    private String userId;
    private String userName;
    private String userEmail;
    private String userPicture;
    private Boolean isUserVerified;
    
    // Thread hierarchy
    private Long parentThreadId;
    private Long rootThreadId;
    private Integer threadLevel;
    private String threadPath;
    
    // Quote/Retweet specific fields
    private Long quotedThreadId;
    private String quoteContent;
    private ThreadDto quotedThread; // Full quoted thread data
    
    // Engagement counts
    private Integer likesCount;
    private Integer repliesCount;
    private Integer repostsCount;
    private Integer sharesCount;
    private Integer viewsCount;
    private Integer bookmarksCount;
    
    // Thread status
    private Boolean isDeleted;
    private Boolean isEdited;
    private Boolean isPinned;
    private Boolean isSensitive;
    private Boolean isPublic;
    
    // Reply permissions
    private String replyPermission;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime editedAt;
    
    // Media attachments
    private List<ThreadMediaDto> media;
    
    // User engagement status
    private Boolean isLiked;
    private Boolean isReposted;
    private Boolean isBookmarked;
    private ReactionType userReaction;
    
    // Hashtags and mentions
    private List<String> hashtags;
    private List<String> mentions;
    
    // Replies (for thread view)
    private List<ThreadDto> replies;
    
    // Poll information
    private ThreadPollDto poll;
    
    // Constructors
    public ThreadDto() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public ThreadType getThreadType() { return threadType; }
    public void setThreadType(ThreadType threadType) { this.threadType = threadType; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public String getUserPicture() { return userPicture; }
    public void setUserPicture(String userPicture) { this.userPicture = userPicture; }
    
    public Boolean getIsUserVerified() { return isUserVerified; }
    public void setIsUserVerified(Boolean isUserVerified) { this.isUserVerified = isUserVerified; }
    
    public Long getParentThreadId() { return parentThreadId; }
    public void setParentThreadId(Long parentThreadId) { this.parentThreadId = parentThreadId; }
    
    public Long getRootThreadId() { return rootThreadId; }
    public void setRootThreadId(Long rootThreadId) { this.rootThreadId = rootThreadId; }
    
    public Integer getThreadLevel() { return threadLevel; }
    public void setThreadLevel(Integer threadLevel) { this.threadLevel = threadLevel; }
    
    public String getThreadPath() { return threadPath; }
    public void setThreadPath(String threadPath) { this.threadPath = threadPath; }
    
    public Long getQuotedThreadId() { return quotedThreadId; }
    public void setQuotedThreadId(Long quotedThreadId) { this.quotedThreadId = quotedThreadId; }
    
    public String getQuoteContent() { return quoteContent; }
    public void setQuoteContent(String quoteContent) { this.quoteContent = quoteContent; }
    
    public ThreadDto getQuotedThread() { return quotedThread; }
    public void setQuotedThread(ThreadDto quotedThread) { this.quotedThread = quotedThread; }
    
    public Integer getLikesCount() { return likesCount; }
    public void setLikesCount(Integer likesCount) { this.likesCount = likesCount; }
    
    public Integer getRepliesCount() { return repliesCount; }
    public void setRepliesCount(Integer repliesCount) { this.repliesCount = repliesCount; }
    
    public Integer getRepostsCount() { return repostsCount; }
    public void setRepostsCount(Integer repostsCount) { this.repostsCount = repostsCount; }
    
    public Integer getSharesCount() { return sharesCount; }
    public void setSharesCount(Integer sharesCount) { this.sharesCount = sharesCount; }
    
    public Integer getViewsCount() { return viewsCount; }
    public void setViewsCount(Integer viewsCount) { this.viewsCount = viewsCount; }
    
    public Integer getBookmarksCount() { return bookmarksCount; }
    public void setBookmarksCount(Integer bookmarksCount) { this.bookmarksCount = bookmarksCount; }
    
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    
    public String getReplyPermission() { return replyPermission; }
    public void setReplyPermission(String replyPermission) { this.replyPermission = replyPermission; }
    
    public Boolean getIsEdited() { return isEdited; }
    public void setIsEdited(Boolean isEdited) { this.isEdited = isEdited; }
    
    public Boolean getIsPinned() { return isPinned; }
    public void setIsPinned(Boolean isPinned) { this.isPinned = isPinned; }
    
    public Boolean getIsSensitive() { return isSensitive; }
    public void setIsSensitive(Boolean isSensitive) { this.isSensitive = isSensitive; }
    
    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getEditedAt() { return editedAt; }
    public void setEditedAt(LocalDateTime editedAt) { this.editedAt = editedAt; }
    
    public List<ThreadMediaDto> getMedia() { return media; }
    public void setMedia(List<ThreadMediaDto> media) { this.media = media; }
    
    public Boolean getIsLiked() { return isLiked; }
    public void setIsLiked(Boolean isLiked) { this.isLiked = isLiked; }
    
    public Boolean getIsReposted() { return isReposted; }
    public void setIsReposted(Boolean isReposted) { this.isReposted = isReposted; }
    
    public Boolean getIsBookmarked() { return isBookmarked; }
    public void setIsBookmarked(Boolean isBookmarked) { this.isBookmarked = isBookmarked; }
    
    public ReactionType getUserReaction() { return userReaction; }
    public void setUserReaction(ReactionType userReaction) { this.userReaction = userReaction; }
    
    public List<String> getHashtags() { return hashtags; }
    public void setHashtags(List<String> hashtags) { this.hashtags = hashtags; }
    
    public List<String> getMentions() { return mentions; }
    public void setMentions(List<String> mentions) { this.mentions = mentions; }
    
    public List<ThreadDto> getReplies() { return replies; }
    public void setReplies(List<ThreadDto> replies) { this.replies = replies; }
    
    public ThreadPollDto getPoll() { return poll; }
    public void setPoll(ThreadPollDto poll) { this.poll = poll; }
}
