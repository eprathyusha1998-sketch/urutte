package com.urutte.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "threads")
public class Thread {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
    
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "thread_type", nullable = false)
    private ThreadType threadType = ThreadType.ORIGINAL;
    
    // Thread hierarchy (unlimited levels)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_thread_id")
    @JsonIgnore
    private Thread parentThread;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "root_thread_id")
    @JsonIgnore
    private Thread rootThread;
    
    @Column(name = "thread_level")
    private Integer threadLevel = 0;
    
    @Column(name = "thread_path", length = 1000)
    private String threadPath;
    
    // Quote/Retweet specific fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quoted_thread_id")
    @JsonIgnore
    private Thread quotedThread;
    
    @Column(name = "quote_content", columnDefinition = "TEXT")
    private String quoteContent;
    
    // Engagement counts
    @Column(name = "likes_count")
    private Integer likesCount = 0;
    
    @Column(name = "replies_count")
    private Integer repliesCount = 0;
    
    @Column(name = "reposts_count")
    private Integer repostsCount = 0;
    
    @Column(name = "shares_count")
    private Integer sharesCount = 0;
    
    @Column(name = "views_count")
    private Integer viewsCount = 0;
    
    @Column(name = "bookmarks_count")
    private Integer bookmarksCount = 0;
    
    // Thread status
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
    
    // Reply permissions
    @Enumerated(EnumType.STRING)
    @Column(name = "reply_permission", nullable = false)
    private ReplyPermission replyPermission = ReplyPermission.ANYONE;
    
    @Column(name = "is_edited")
    private Boolean isEdited = false;
    
    @Column(name = "is_pinned")
    private Boolean isPinned = false;
    
    @Column(name = "is_sensitive")
    private Boolean isSensitive = false;
    
    @Column(name = "is_public")
    private Boolean isPublic = true;
    
    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "edited_at")
    private LocalDateTime editedAt;
    
    // Relationships
    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ThreadMedia> media = new ArrayList<>();
    
    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ThreadLike> likes = new ArrayList<>();
    
    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ThreadRepost> reposts = new ArrayList<>();
    
    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ThreadBookmark> bookmarks = new ArrayList<>();
    
    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ThreadReaction> reactions = new ArrayList<>();
    
    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ThreadView> views = new ArrayList<>();
    
    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ThreadMention> mentions = new ArrayList<>();
    
    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ThreadHashtag> hashtags = new ArrayList<>();
    
    @OneToMany(mappedBy = "parentThread", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Thread> replies = new ArrayList<>();
    
    @OneToMany(mappedBy = "rootThread", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Thread> allReplies = new ArrayList<>();
    
    // Constructors
    public Thread() {}
    
    public Thread(String content, User user) {
        this.content = content;
        this.user = user;
        this.threadType = ThreadType.ORIGINAL;
        this.threadLevel = 0;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public ThreadType getThreadType() { return threadType; }
    public void setThreadType(ThreadType threadType) { this.threadType = threadType; }
    
    public Thread getParentThread() { return parentThread; }
    public void setParentThread(Thread parentThread) { this.parentThread = parentThread; }
    
    public Thread getRootThread() { return rootThread; }
    public void setRootThread(Thread rootThread) { this.rootThread = rootThread; }
    
    public Integer getThreadLevel() { return threadLevel; }
    public void setThreadLevel(Integer threadLevel) { this.threadLevel = threadLevel; }
    
    public String getThreadPath() { return threadPath; }
    public void setThreadPath(String threadPath) { this.threadPath = threadPath; }
    
    public Thread getQuotedThread() { return quotedThread; }
    public void setQuotedThread(Thread quotedThread) { this.quotedThread = quotedThread; }
    
    public String getQuoteContent() { return quoteContent; }
    public void setQuoteContent(String quoteContent) { this.quoteContent = quoteContent; }
    
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
    
    public ReplyPermission getReplyPermission() { return replyPermission; }
    public void setReplyPermission(ReplyPermission replyPermission) { this.replyPermission = replyPermission; }
    
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
    
    public List<ThreadMedia> getMedia() { return media; }
    public void setMedia(List<ThreadMedia> media) { this.media = media; }
    
    public List<ThreadLike> getLikes() { return likes; }
    public void setLikes(List<ThreadLike> likes) { this.likes = likes; }
    
    public List<ThreadRepost> getReposts() { return reposts; }
    public void setReposts(List<ThreadRepost> reposts) { this.reposts = reposts; }
    
    public List<ThreadBookmark> getBookmarks() { return bookmarks; }
    public void setBookmarks(List<ThreadBookmark> bookmarks) { this.bookmarks = bookmarks; }
    
    public List<ThreadReaction> getReactions() { return reactions; }
    public void setReactions(List<ThreadReaction> reactions) { this.reactions = reactions; }
    
    public List<ThreadView> getViews() { return views; }
    public void setViews(List<ThreadView> views) { this.views = views; }
    
    public List<ThreadMention> getMentions() { return mentions; }
    public void setMentions(List<ThreadMention> mentions) { this.mentions = mentions; }
    
    public List<ThreadHashtag> getHashtags() { return hashtags; }
    public void setHashtags(List<ThreadHashtag> hashtags) { this.hashtags = hashtags; }
    
    public List<Thread> getReplies() { return replies; }
    public void setReplies(List<Thread> replies) { this.replies = replies; }
    
    public List<Thread> getAllReplies() { return allReplies; }
    public void setAllReplies(List<Thread> allReplies) { this.allReplies = allReplies; }
    
    // Helper methods
    public boolean isMainThread() {
        return parentThread == null;
    }
    
    public boolean isReply() {
        return parentThread != null;
    }
    
    public boolean isQuoteRepost() {
        return threadType == ThreadType.QUOTE;
    }
    
    public void incrementLikesCount() {
        this.likesCount = (this.likesCount == null ? 0 : this.likesCount) + 1;
    }
    
    public void decrementLikesCount() {
        this.likesCount = Math.max(0, (this.likesCount == null ? 0 : this.likesCount) - 1);
    }
    
    public void incrementRepostsCount() {
        this.repostsCount = (this.repostsCount == null ? 0 : this.repostsCount) + 1;
    }
    
    public void decrementRepostsCount() {
        this.repostsCount = Math.max(0, (this.repostsCount == null ? 0 : this.repostsCount) - 1);
    }
    
    public void incrementRepliesCount() {
        this.repliesCount = (this.repliesCount == null ? 0 : this.repliesCount) + 1;
    }
    
    public void decrementRepliesCount() {
        this.repliesCount = Math.max(0, (this.repliesCount == null ? 0 : this.repliesCount) - 1);
    }
}
