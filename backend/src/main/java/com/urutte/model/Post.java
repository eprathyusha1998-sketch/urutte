package com.urutte.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 2000)
    private String content;
    
    private Instant timestamp;
    private String mediaUrl;
    private String mediaType;
    
    // Counters (denormalized for performance)
    private Integer likesCount = 0;
    private Integer commentsCount = 0;
    private Integer repostsCount = 0;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_post_id")
    private Post parentPost;
    
    // Thread hierarchy fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "root_post_id")
    private Post rootPost; // Points to the main thread post
    
    @Column(name = "thread_level")
    private Integer threadLevel = 0; // 0 = main post, 1 = first level reply, etc.
    
    @Column(name = "thread_path")
    private String threadPath; // e.g., "1.2.3" for hierarchical path
    
    // Quote repost fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quoted_post_id")
    private Post quotedPost; // Original post being quoted
    
    @Column(name = "is_quote_repost")
    private Boolean isQuoteRepost = false;
    
    @OneToMany(mappedBy = "parentPost", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Post> replies;
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Like> likes;
    
    @OneToMany(mappedBy = "originalPost", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Repost> reposts;
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments;

    // Constructors
    public Post() {
        this.timestamp = Instant.now();
    }

    public Post(String content, User user) {
        this();
        this.content = content;
        this.user = user;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Post getParentPost() { return parentPost; }
    public void setParentPost(Post parentPost) { this.parentPost = parentPost; }

    public List<Post> getReplies() { return replies; }
    public void setReplies(List<Post> replies) { this.replies = replies; }

    public List<Like> getLikes() { return likes; }
    public void setLikes(List<Like> likes) { this.likes = likes; }

    public List<Repost> getReposts() { return reposts; }
    public void setReposts(List<Repost> reposts) { this.reposts = reposts; }
    
    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }
    
    public Integer getLikesCount() { return likesCount; }
    public void setLikesCount(Integer likesCount) { this.likesCount = likesCount; }
    
    public Integer getCommentsCount() { return commentsCount; }
    public void setCommentsCount(Integer commentsCount) { this.commentsCount = commentsCount; }
    
    public Integer getRepostsCount() { return repostsCount; }
    public void setRepostsCount(Integer repostsCount) { this.repostsCount = repostsCount; }
    
    // Thread hierarchy getters and setters
    public Post getRootPost() { return rootPost; }
    public void setRootPost(Post rootPost) { this.rootPost = rootPost; }
    
    public Integer getThreadLevel() { return threadLevel; }
    public void setThreadLevel(Integer threadLevel) { this.threadLevel = threadLevel; }
    
    public String getThreadPath() { return threadPath; }
    public void setThreadPath(String threadPath) { this.threadPath = threadPath; }
    
    // Quote repost getters and setters
    public Post getQuotedPost() { return quotedPost; }
    public void setQuotedPost(Post quotedPost) { this.quotedPost = quotedPost; }
    
    public Boolean getIsQuoteRepost() { return isQuoteRepost; }
    public void setIsQuoteRepost(Boolean isQuoteRepost) { this.isQuoteRepost = isQuoteRepost; }
}
