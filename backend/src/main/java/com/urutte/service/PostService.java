package com.urutte.service;

import com.urutte.dto.CreatePostDto;
import com.urutte.dto.PostDto;
import com.urutte.model.Like;
import com.urutte.model.Post;
import com.urutte.model.Repost;
import com.urutte.model.User;
import com.urutte.repository.CommentRepository;
import com.urutte.repository.LikeRepository;
import com.urutte.repository.PostRepository;
import com.urutte.repository.RepostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostService {
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private LikeRepository likeRepository;
    
    @Autowired
    private RepostRepository repostRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;
    
    public PostDto createPost(CreatePostDto createPostDto, String userId) {
        User user = userService.getUserById(userId);
        
        Post post = new Post();
        post.setContent(createPostDto.getContent());
        post.setUser(user);
        post.setMediaUrl(createPostDto.getMediaUrl());
        post.setMediaType(createPostDto.getMediaType());
        
        // Handle replies and thread hierarchy
        if (createPostDto.getParentPostId() != null) {
            Post parentPost = postRepository.findById(createPostDto.getParentPostId())
                .orElseThrow(() -> new RuntimeException("Parent post not found"));
            post.setParentPost(parentPost);
            
            // Set thread hierarchy
            Post rootPost = parentPost.getRootPost() != null ? parentPost.getRootPost() : parentPost;
            post.setRootPost(rootPost);
            
            // Handle null threadLevel for existing posts
            Integer parentThreadLevel = parentPost.getThreadLevel();
            if (parentThreadLevel == null) {
                parentThreadLevel = 0; // Default to 0 for existing posts
            }
            post.setThreadLevel(parentThreadLevel + 1);
            
            // Build thread path
            String parentPath = parentPost.getThreadPath() != null ? parentPost.getThreadPath() : String.valueOf(parentPost.getId());
            post.setThreadPath(parentPath + "." + parentPost.getId());
        } else {
            // This is a main post
            post.setRootPost(null);
            post.setThreadLevel(0);
            post.setThreadPath(null);
        }
        
        Post savedPost = postRepository.save(post);
        return convertToDto(savedPost, userId);
    }
    
    public PostDto createQuoteRepost(String content, Long quotedPostId, String mediaUrl, String mediaType, String userId) {
        User user = userService.getUserById(userId);
        Post quotedPost = postRepository.findById(quotedPostId)
            .orElseThrow(() -> new RuntimeException("Quoted post not found"));
        
        Post post = new Post();
        post.setContent(content);
        post.setUser(user);
        post.setMediaUrl(mediaUrl);
        post.setMediaType(mediaType);
        post.setQuotedPost(quotedPost);
        post.setIsQuoteRepost(true);
        
        // Quote reposts are always main posts (not replies)
        post.setRootPost(null);
        post.setThreadLevel(0);
        post.setThreadPath(null);
        post.setParentPost(null);
        
        Post savedPost = postRepository.save(post);
        return convertToDto(savedPost, userId);
    }
    
    public Page<PostDto> getFeed(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findFeedPosts(userId, pageable);
        
        // If no posts from followed users, show all posts (for new users)
        if (posts.isEmpty()) {
            posts = postRepository.findByParentPostIsNullOrderByTimestampDesc(pageable);
        }
        
        return posts.map(post -> convertToDto(post, userId));
    }
    
    public Page<PostDto> getAllPosts(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findByParentPostIsNullOrderByTimestampDesc(pageable);
        return posts.map(post -> convertToDto(post, userId));
    }
    
    public List<PostDto> getReplies(Long postId, String userId) {
        Post rootPost = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        
        // Get all replies in the thread hierarchy
        List<Post> allReplies = postRepository.findByRootPostOrderByThreadPathAsc(rootPost);
        
        // Filter out the root post itself and return only replies
        return allReplies.stream()
            .filter(post -> !post.getId().equals(postId))
            .map(post -> convertToDto(post, userId))
            .collect(Collectors.toList());
    }
    
    public PostDto likePost(Long postId, String userId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        
        User user = userService.getUserById(userId);
        
        // Check if already liked
        if (likeRepository.existsByUserIdAndPostId(userId, postId)) {
            likeRepository.deleteByUserIdAndPostId(userId, postId);
        } else {
            Like like = new Like(user, post);
            likeRepository.save(like);
            
            // Create notification for post author (if not the same user)
            if (!post.getUser().getId().equals(userId)) {
                notificationService.createNotification(
                    post.getUser().getId(),
                    userId,
                    "like",
                    "New Like",
                    user.getName() + " liked your post",
                    "post",
                    postId
                );
            }
        }
        
        return convertToDto(post, userId);
    }
    
    public PostDto repost(Long postId, String userId) {
        Post originalPost = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        
        User user = userService.getUserById(userId);
        
        // Check if already reposted
        if (repostRepository.existsByUserIdAndOriginalPostId(userId, postId)) {
            repostRepository.deleteByUserIdAndOriginalPostId(userId, postId);
        } else {
            Repost repost = new Repost(user, originalPost);
            repostRepository.save(repost);
            
            // Create notification for original post author (if not the same user)
            if (!originalPost.getUser().getId().equals(userId)) {
                notificationService.createNotification(
                    originalPost.getUser().getId(),
                    userId,
                    "repost",
                    "New Repost",
                    user.getName() + " reposted your post",
                    "post",
                    postId
                );
            }
        }
        
        return convertToDto(originalPost, userId);
    }
    
    public Page<PostDto> searchPosts(String keyword, String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findByContentContaining(keyword, pageable);
        return posts.map(post -> convertToDto(post, userId));
    }
    
    public PostDto getPostById(Long postId, String currentUserId) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isPresent()) {
            return convertToDto(postOpt.get(), currentUserId);
        }
        throw new RuntimeException("Post not found with id: " + postId);
    }
    
    private PostDto convertToDto(Post post, String currentUserId) {
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        dto.setContent(post.getContent());
        dto.setTimestamp(post.getTimestamp());
        dto.setUserId(post.getUser().getId());
        dto.setUserName(post.getUser().getName());
        dto.setUserEmail(post.getUser().getEmail());
        dto.setUserPicture(post.getUser().getPicture());
        dto.setMediaUrl(post.getMediaUrl());
        dto.setMediaType(post.getMediaType());
        dto.setParentPostId(post.getParentPost() != null ? post.getParentPost().getId() : null);
        
        // Set thread hierarchy fields
        dto.setRootPostId(post.getRootPost() != null ? post.getRootPost().getId() : null);
        dto.setThreadLevel(post.getThreadLevel() != null ? post.getThreadLevel() : 0);
        dto.setThreadPath(post.getThreadPath());
        
        // Set quote repost fields
        dto.setQuotedPostId(post.getQuotedPost() != null ? post.getQuotedPost().getId() : null);
        dto.setIsQuoteRepost(post.getIsQuoteRepost() != null ? post.getIsQuoteRepost() : false);
        
        // If this is a quote repost, include the quoted post data
        if (post.getIsQuoteRepost() != null && post.getIsQuoteRepost() && post.getQuotedPost() != null) {
            dto.setQuotedPost(convertToDto(post.getQuotedPost(), currentUserId));
        }
        
        // Set interaction counts
        dto.setLikes(likeRepository.countByPost(post));
        dto.setRetweets(repostRepository.countByOriginalPost(post));
        dto.setReposts(repostRepository.countByOriginalPost(post)); // Add reposts count
        dto.setReplies(postRepository.countByParentPost(post));
        // Count only main comments (excluding replies)
        dto.setCommentsCount(commentRepository.countByPostAndParentCommentIsNull(post));
        
        // Set user interaction status
        dto.setLiked(likeRepository.existsByUserIdAndPostId(currentUserId, post.getId()));
        dto.setRetweeted(repostRepository.existsByUserIdAndOriginalPostId(currentUserId, post.getId()));
        dto.setReposted(repostRepository.existsByUserIdAndOriginalPostId(currentUserId, post.getId())); // Add reposted status
        
        return dto;
    }
    
    public boolean deletePost(Long postId, String userId) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            // Check if user owns the post
            if (post.getUser().getId().equals(userId)) {
                // Delete all replies first (cascade delete)
                List<Post> replies = postRepository.findByRootPostOrderByThreadPathAsc(post);
                for (Post reply : replies) {
                    if (!reply.getId().equals(postId)) { // Don't delete the main post yet
                        deletePostCascade(reply);
                    }
                }
                
                // Delete the main post
                deletePostCascade(post);
                return true;
            }
        }
        return false;
    }
    
    private void deletePostCascade(Post post) {
        // Delete all likes for this post
        likeRepository.deleteByPost(post);
        
        // Delete all reposts for this post
        repostRepository.deleteByOriginalPost(post);
        
        // Delete all comments (replies) for this post
        commentRepository.deleteByPost(post);
        
        // Finally delete the post itself
        postRepository.delete(post);
    }
    
    /**
     * Migration method to fix thread hierarchy for existing posts
     * This should be called once during deployment to fix existing data
     */
    @Transactional
    public void migrateThreadHierarchy() {
        // Update main posts (posts without parent)
        List<Post> mainPosts = postRepository.findByParentPostIsNullOrderByTimestampDesc(PageRequest.of(0, 1000)).getContent();
        for (Post post : mainPosts) {
            if (post.getThreadLevel() == null) {
                post.setThreadLevel(0);
                post.setRootPost(null);
                post.setThreadPath(null);
                postRepository.save(post);
            }
        }
        
        // Update replies
        List<Post> allPosts = postRepository.findAll();
        for (Post post : allPosts) {
            if (post.getParentPost() != null && post.getThreadLevel() == null) {
                Post parent = post.getParentPost();
                
                // Set thread level
                Integer parentLevel = parent.getThreadLevel();
                if (parentLevel == null) {
                    parentLevel = 0;
                }
                post.setThreadLevel(parentLevel + 1);
                
                // Set root post
                Post rootPost = parent.getRootPost() != null ? parent.getRootPost() : parent;
                post.setRootPost(rootPost);
                
                // Set thread path
                String parentPath = parent.getThreadPath() != null ? parent.getThreadPath() : String.valueOf(parent.getId());
                post.setThreadPath(parentPath + "." + parent.getId());
                
                postRepository.save(post);
            }
        }
    }
}
