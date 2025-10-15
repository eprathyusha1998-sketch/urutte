package com.urutte.service;

import com.urutte.dto.ThreadDto;
import com.urutte.dto.ThreadMediaDto;
import com.urutte.dto.ThreadPollDto;
import com.urutte.dto.PollOptionDto;
import com.urutte.exception.ThreadAccessDeniedException;
import com.urutte.model.*;
import com.urutte.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class ThreadService {
    
    @Autowired
    private ThreadRepository threadRepository;
    
    @Autowired
    private ThreadLikeRepository threadLikeRepository;
    
    @Autowired
    private ThreadRepostRepository threadRepostRepository;
    
    @Autowired
    private ThreadBookmarkRepository threadBookmarkRepository;
    
    @Autowired
    private ThreadReactionRepository threadReactionRepository;
    
    @Autowired
    private ThreadViewRepository threadViewRepository;
    
    @Autowired
    private ThreadMentionRepository threadMentionRepository;
    
    @Autowired
    private FollowRepository followRepository;
    
    @Autowired
    private HashtagRepository hashtagRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ThreadHashtagRepository threadHashtagRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Create a new thread
    public ThreadDto createThread(String content, String userId, Long parentThreadId, String mediaUrl, String mediaType, String replyPermission) {
        return createThreadWithMedia(content, userId, parentThreadId, mediaUrl != null ? List.of(mediaUrl) : null, mediaType != null ? List.of(mediaType) : null, replyPermission);
    }
    
    // Create a new thread with multiple media files
    public ThreadDto createThreadWithMedia(String content, String userId, Long parentThreadId, List<String> mediaUrls, List<String> mediaTypes, String replyPermission) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        com.urutte.model.Thread thread = new com.urutte.model.Thread(content, user);
        
        // Set reply permission and view privacy
        if (replyPermission != null && !replyPermission.isEmpty()) {
            try {
                ReplyPermission permission = ReplyPermission.valueOf(replyPermission.toUpperCase());
                thread.setReplyPermission(permission);
                
                // Set view privacy based on reply permission
                switch (permission) {
                    case ANYONE:
                        thread.setIsPublic(true); // Public - anyone can view
                        break;
                    case FOLLOWERS:
                        thread.setIsPublic(false); // Private - only followers can view
                        break;
                    case FOLLOWING:
                        thread.setIsPublic(false); // Private - only people I follow can view
                        break;
                    case MENTIONED_ONLY:
                        thread.setIsPublic(false); // Private - only mentioned users can view
                        break;
                    default:
                        thread.setIsPublic(true);
                        break;
                }
            } catch (IllegalArgumentException e) {
                // Default to ANYONE if invalid permission
                thread.setReplyPermission(ReplyPermission.ANYONE);
                thread.setIsPublic(true);
            }
        } else {
            thread.setReplyPermission(ReplyPermission.ANYONE);
            thread.setIsPublic(true);
        }
        
        // Handle replies and thread hierarchy
        if (parentThreadId != null) {
            com.urutte.model.Thread parentThread = threadRepository.findById(parentThreadId)
                .orElseThrow(() -> new RuntimeException("Parent thread not found"));
            
            thread.setParentThread(parentThread);
            thread.setThreadType(ThreadType.REPLY);
            
            // Set thread hierarchy
            com.urutte.model.Thread rootThread = parentThread.getRootThread() != null ? parentThread.getRootThread() : parentThread;
            thread.setRootThread(rootThread);
            thread.setThreadLevel(parentThread.getThreadLevel() + 1);
            
            // Save the thread first to get the ID
            thread = threadRepository.save(thread);
            
            // Build thread path - this represents the path from root to the current thread's parent
            if (parentThread.getThreadPath() != null) {
                // Parent has a path, append parent's ID to it
                thread.setThreadPath(parentThread.getThreadPath() + "." + parentThread.getId());
            } else {
                // Parent is a main thread, path is just the parent's ID
                thread.setThreadPath(String.valueOf(parentThread.getId()));
            }
            
            // Save again with the correct thread path
            thread = threadRepository.save(thread);
        } else {
            // This is a main thread
            thread.setRootThread(null);
            thread.setThreadLevel(0);
            thread.setThreadPath(null);
            
            // Save the thread
            thread = threadRepository.save(thread);
        }
        
        // If this is a reply, increment the parent thread's replies count
        if (parentThreadId != null) {
            com.urutte.model.Thread parentThread = threadRepository.findById(parentThreadId).orElse(null);
            if (parentThread != null) {
                parentThread.setRepliesCount(parentThread.getRepliesCount() + 1);
                threadRepository.save(parentThread);
            }
        }
        
        // Add media if provided
        if (mediaUrls != null && !mediaUrls.isEmpty()) {
            addMultipleMediaToThread(thread, mediaUrls, mediaTypes);
        }
        
        // Process hashtags and mentions
        processHashtagsAndMentions(thread, content);
        
        return convertToDto(thread, userId);
    }
    
    // Create a quote repost
    public ThreadDto createQuoteRepost(String content, String userId, Long quotedThreadId, String mediaUrl, String mediaType) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        com.urutte.model.Thread quotedThread = threadRepository.findById(quotedThreadId)
            .orElseThrow(() -> new RuntimeException("Quoted thread not found"));
        
        com.urutte.model.Thread thread = new com.urutte.model.Thread(content, user);
        thread.setThreadType(ThreadType.QUOTE);
        thread.setQuotedThread(quotedThread);
        thread.setQuoteContent(content);
        thread.setThreadLevel(0); // Quote reposts are always main threads
        thread.setRootThread(null);
        thread.setThreadPath(null);
        
        // Save the thread
        thread = threadRepository.save(thread);
        
        // Add media if provided
        if (mediaUrl != null && !mediaUrl.isEmpty()) {
            addMediaToThread(thread, mediaUrl, mediaType);
        }
        
        // Process hashtags and mentions
        processHashtagsAndMentions(thread, content);
        
        return convertToDto(thread, userId);
    }
    
    // Get main threads (feed)
    public Page<ThreadDto> getMainThreads(String currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        
        // Get public threads and followers-only threads for current user
        Page<com.urutte.model.Thread> threads;
        
        if (currentUserId != null && !currentUserId.isEmpty()) {
            // Get public threads + followers-only threads from users that current user follows
            threads = threadRepository.findMainThreadsForUser(currentUserId, pageable);
        } else {
            // For anonymous users, only show public threads
            threads = threadRepository.findByParentThreadIsNullAndIsDeletedFalseAndIsPublicTrueOrderByCreatedAtDesc(pageable);
        }
        
        return threads.map(thread -> convertToDto(thread, currentUserId));
    }
    
    // Get user's own threads
    public Page<ThreadDto> getUserThreads(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Page<com.urutte.model.Thread> threads = threadRepository.findByUserAndIsDeletedFalseOrderByCreatedAtDesc(user, pageable);
        
        return threads.map(thread -> convertToDto(thread, userId));
    }
    
    // Edit a thread
    public ThreadDto editThread(Long threadId, String newContent, String userId) {
        com.urutte.model.Thread thread = threadRepository.findById(threadId)
            .orElseThrow(() -> new RuntimeException("Thread not found"));
        
        // Check if the user owns this thread
        if (!thread.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only edit your own threads");
        }
        
        if (thread.getIsDeleted()) {
            throw new RuntimeException("Cannot edit deleted thread");
        }
        
        // Update the content
        thread.setContent(newContent);
        thread.setIsEdited(true);
        thread.setEditedAt(LocalDateTime.now());
        
        // Save the updated thread
        thread = threadRepository.save(thread);
        
        return convertToDto(thread, userId);
    }
    
    // Get thread by ID
    public ThreadDto getThreadById(Long threadId, String currentUserId) {
        com.urutte.model.Thread thread = threadRepository.findById(threadId)
            .orElseThrow(() -> new RuntimeException("Thread not found"));
        
        if (thread.getIsDeleted()) {
            throw new RuntimeException("Thread not found");
        }
        
        // Check if user has permission to view this thread
        if (!thread.getIsPublic()) {
            if (currentUserId == null || currentUserId.isEmpty()) {
                throw new ThreadAccessDeniedException("You must be logged in to view this thread");
            }
            
            boolean hasAccess = false;
            
            switch (thread.getReplyPermission()) {
                case FOLLOWERS:
                    // Check if current user follows the thread author
                    hasAccess = followRepository.existsByFollowerIdAndFollowingId(
                        currentUserId, thread.getUser().getId());
                    break;
                    
                case FOLLOWING:
                    // Check if thread author follows the current user
                    hasAccess = followRepository.existsByFollowerIdAndFollowingId(
                        thread.getUser().getId(), currentUserId);
                    break;
                    
                case MENTIONED_ONLY:
                    // Check if current user is mentioned in this thread
                    hasAccess = threadMentionRepository.existsByThreadAndMentionedUserId(
                        thread, currentUserId);
                    break;
                    
                default:
                    hasAccess = false;
                    break;
            }
            
            if (!hasAccess) {
                throw new ThreadAccessDeniedException("You don't have permission to view this thread");
            }
        }
        
        return convertToDto(thread, currentUserId);
    }
    
    // Get replies for a thread
    public List<ThreadDto> getThreadReplies(Long threadId, String currentUserId) {
        // Verify the thread exists
        if (!threadRepository.existsById(threadId)) {
            throw new RuntimeException("Thread not found");
        }
        
        // Find direct replies to this specific thread (where parentThreadId = threadId)
        List<com.urutte.model.Thread> replies = threadRepository.findByParentThreadIdAndIsDeletedFalseOrderByCreatedAtAsc(threadId);
        
        return replies.stream()
            .map(reply -> convertToDto(reply, currentUserId))
            .collect(Collectors.toList());
    }
    
    // Like a thread
    public boolean likeThread(Long threadId, String userId) {
        com.urutte.model.Thread thread = threadRepository.findById(threadId)
            .orElseThrow(() -> new RuntimeException("Thread not found"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Optional<ThreadLike> existingLike = threadLikeRepository.findByThreadAndUser(thread, user);
        
        if (existingLike.isPresent()) {
            // Unlike
            threadLikeRepository.delete(existingLike.get());
            // Decrement count
            thread.setLikesCount(Math.max(0, thread.getLikesCount() - 1));
            threadRepository.save(thread);
            return false;
        } else {
            // Like
            ThreadLike like = new ThreadLike(thread, user);
            threadLikeRepository.save(like);
            // Increment count
            thread.setLikesCount(thread.getLikesCount() + 1);
            threadRepository.save(thread);
            return true;
        }
    }
    
    // Repost a thread
    public boolean repostThread(Long threadId, String userId, String quoteContent) {
        com.urutte.model.Thread thread = threadRepository.findById(threadId)
            .orElseThrow(() -> new RuntimeException("Thread not found"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Optional<ThreadRepost> existingRepost = threadRepostRepository.findByThreadAndUser(thread, user);
        
        if (existingRepost.isPresent()) {
            // Unrepost
            threadRepostRepository.delete(existingRepost.get());
            // Decrement count
            thread.setRepostsCount(Math.max(0, thread.getRepostsCount() - 1));
            threadRepository.save(thread);
            return false;
        } else {
            // Repost
            RepostType repostType = (quoteContent != null && !quoteContent.trim().isEmpty()) 
                ? RepostType.QUOTE 
                : RepostType.REPOST;
            
            ThreadRepost repost = new ThreadRepost(thread, user, repostType, quoteContent);
            threadRepostRepository.save(repost);
            // Increment count
            thread.setRepostsCount(thread.getRepostsCount() + 1);
            threadRepository.save(thread);
            return true;
        }
    }
    
    // Bookmark a thread
    public boolean bookmarkThread(Long threadId, String userId) {
        com.urutte.model.Thread thread = threadRepository.findById(threadId)
            .orElseThrow(() -> new RuntimeException("Thread not found"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Optional<ThreadBookmark> existingBookmark = threadBookmarkRepository.findByThreadAndUser(thread, user);
        
        if (existingBookmark.isPresent()) {
            // Remove bookmark
            threadBookmarkRepository.delete(existingBookmark.get());
            return false;
        } else {
            // Add bookmark
            ThreadBookmark bookmark = new ThreadBookmark(thread, user);
            threadBookmarkRepository.save(bookmark);
            return true;
        }
    }
    
    // Add reaction to thread
    public boolean addReaction(Long threadId, String userId, ReactionType reactionType) {
        com.urutte.model.Thread thread = threadRepository.findById(threadId)
            .orElseThrow(() -> new RuntimeException("Thread not found"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Optional<ThreadReaction> existingReaction = threadReactionRepository.findByThreadAndUser(thread, user);
        
        if (existingReaction.isPresent()) {
            ThreadReaction reaction = existingReaction.get();
            if (reaction.getReactionType() == reactionType) {
                // Remove reaction
                threadReactionRepository.delete(reaction);
                return false;
            } else {
                // Update reaction
                reaction.setReactionType(reactionType);
                threadReactionRepository.save(reaction);
                return true;
            }
        } else {
            // Add new reaction
            ThreadReaction reaction = new ThreadReaction(thread, user, reactionType);
            threadReactionRepository.save(reaction);
            return true;
        }
    }
    
    // Delete a thread
    public boolean deleteThread(Long threadId, String userId) {
        com.urutte.model.Thread thread = threadRepository.findById(threadId)
            .orElseThrow(() -> new RuntimeException("Thread not found"));
        
        if (!thread.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this thread");
        }
        
        // Soft delete
        thread.setIsDeleted(true);
        threadRepository.save(thread);
        
        return true;
    }
    
    // Search threads
    public Page<ThreadDto> searchThreads(String keyword, String currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<com.urutte.model.Thread> threads = threadRepository.findByContentContaining(keyword, pageable);
        
        return threads.map(thread -> convertToDto(thread, currentUserId));
    }
    
    // Get threads by hashtag
    public Page<ThreadDto> getThreadsByHashtag(String hashtag, String currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<com.urutte.model.Thread> threads = threadRepository.findByHashtag(hashtag, pageable);
        
        return threads.map(thread -> convertToDto(thread, currentUserId));
    }
    
    // Get trending threads
    public Page<ThreadDto> getTrendingThreads(String currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        Page<com.urutte.model.Thread> threads = threadRepository.findTrendingThreads(since, pageable);
        
        return threads.map(thread -> convertToDto(thread, currentUserId));
    }
    
    // Get liked threads by user
    public List<ThreadDto> getLikedThreadsByUser(String userId, int limit) {
        User user = userService.getUserById(userId);
        List<com.urutte.model.Thread> likedThreads = threadLikeRepository.findThreadsLikedByUser(user);
        
        // Limit the results
        if (likedThreads.size() > limit) {
            likedThreads = likedThreads.subList(0, limit);
        }
        
        return likedThreads.stream()
                .map(thread -> convertToDto(thread, userId))
                .collect(Collectors.toList());
    }
    
    // Helper method to add media to thread
    private void addMediaToThread(com.urutte.model.Thread thread, String mediaUrl, String mediaType) {
        MediaType type = MediaType.IMAGE; // Default
        if (mediaType != null) {
            try {
                // Convert lowercase string to enum
                switch (mediaType.toLowerCase()) {
                    case "image":
                        type = MediaType.IMAGE;
                        break;
                    case "video":
                        type = MediaType.VIDEO;
                        break;
                    case "gif":
                        type = MediaType.GIF;
                        break;
                    case "audio":
                        type = MediaType.AUDIO;
                        break;
                    case "document":
                        type = MediaType.DOCUMENT;
                        break;
                    default:
                        type = MediaType.IMAGE; // Default fallback
                        break;
                }
            } catch (Exception e) {
                // Keep default
                type = MediaType.IMAGE;
            }
        }
        
        ThreadMedia media = new ThreadMedia(thread, type, mediaUrl);
        thread.getMedia().add(media);
    }
    
    // Helper method to add multiple media files to thread
    private void addMultipleMediaToThread(com.urutte.model.Thread thread, List<String> mediaUrls, List<String> mediaTypes) {
        for (int i = 0; i < mediaUrls.size(); i++) {
            String mediaUrl = mediaUrls.get(i);
            String mediaType = (mediaTypes != null && i < mediaTypes.size()) ? mediaTypes.get(i) : "image";
            
            MediaType type = MediaType.IMAGE; // Default
            if (mediaType != null) {
                try {
                    // Convert lowercase string to enum
                    switch (mediaType.toLowerCase()) {
                        case "image":
                            type = MediaType.IMAGE;
                            break;
                        case "video":
                            type = MediaType.VIDEO;
                            break;
                        case "gif":
                            type = MediaType.GIF;
                            break;
                        case "audio":
                            type = MediaType.AUDIO;
                            break;
                        case "document":
                            type = MediaType.DOCUMENT;
                            break;
                        default:
                            type = MediaType.IMAGE; // Default fallback
                            break;
                    }
                } catch (Exception e) {
                    // Keep default
                    type = MediaType.IMAGE;
                }
            }
            
            ThreadMedia media = new ThreadMedia(thread, type, mediaUrl);
            media.setDisplayOrder(i); // Set display order
            thread.getMedia().add(media);
        }
    }
    
    // Helper method to process hashtags and mentions
    private void processHashtagsAndMentions(com.urutte.model.Thread thread, String content) {
        if (content == null || content.isEmpty()) return;
        
        // Process hashtags
        Pattern hashtagPattern = Pattern.compile("#(\\w+)");
        Matcher hashtagMatcher = hashtagPattern.matcher(content);
        
        while (hashtagMatcher.find()) {
            String tag = hashtagMatcher.group(1).toLowerCase();
            Hashtag hashtag = hashtagRepository.findByTag(tag)
                .orElseGet(() -> {
                    Hashtag newHashtag = new Hashtag(tag);
                    return hashtagRepository.save(newHashtag);
                });
            
            hashtag.incrementUsageCount();
            hashtagRepository.save(hashtag);
            
            // Only create the relationship if it doesn't already exist
            if (!threadHashtagRepository.existsByThreadAndHashtag(thread, hashtag)) {
                ThreadHashtag threadHashtag = new ThreadHashtag(thread, hashtag);
                threadHashtagRepository.save(threadHashtag);
            }
        }
        
        // Process mentions
        Pattern mentionPattern = Pattern.compile("@(\\w+)");
        Matcher mentionMatcher = mentionPattern.matcher(content);
        
        while (mentionMatcher.find()) {
            String username = mentionMatcher.group(1);
            Optional<User> mentionedUser = userRepository.findByUsername(username);
            
            if (mentionedUser.isPresent()) {
                ThreadMention mention = new ThreadMention(
                    thread, 
                    mentionedUser.get(), 
                    mentionMatcher.start(), 
                    mentionMatcher.end()
                );
                threadMentionRepository.save(mention);
            }
        }
    }
    
    // Convert Thread entity to DTO
    private ThreadDto convertToDto(com.urutte.model.Thread thread, String currentUserId) {
        ThreadDto dto = new ThreadDto();
        
        // Basic thread info
        dto.setId(thread.getId());
        dto.setContent(thread.getContent());
        dto.setThreadType(thread.getThreadType());
        
        // User info
        dto.setUserId(thread.getUser().getId());
        dto.setUserName(thread.getUser().getName());
        dto.setUserEmail(thread.getUser().getEmail());
        dto.setUserPicture(thread.getUser().getPicture());
        dto.setIsUserVerified(thread.getUser().getIsVerified());
        
        // Thread hierarchy
        dto.setParentThreadId(thread.getParentThread() != null ? thread.getParentThread().getId() : null);
        dto.setRootThreadId(thread.getRootThread() != null ? thread.getRootThread().getId() : null);
        dto.setThreadLevel(thread.getThreadLevel());
        dto.setThreadPath(thread.getThreadPath());
        
        // Quote info
        dto.setQuotedThreadId(thread.getQuotedThread() != null ? thread.getQuotedThread().getId() : null);
        dto.setQuoteContent(thread.getQuoteContent());
        
        // Engagement counts
        dto.setLikesCount(thread.getLikesCount());
        dto.setRepliesCount(thread.getRepliesCount());
        dto.setRepostsCount(thread.getRepostsCount());
        dto.setSharesCount(thread.getSharesCount());
        dto.setViewsCount(thread.getViewsCount());
        dto.setBookmarksCount(thread.getBookmarksCount());
        
        // Status
        dto.setIsDeleted(thread.getIsDeleted());
        dto.setIsEdited(thread.getIsEdited());
        dto.setIsPinned(thread.getIsPinned());
        dto.setIsSensitive(thread.getIsSensitive());
        dto.setIsPublic(thread.getIsPublic());
        
        // Reply permissions
        dto.setReplyPermission(thread.getReplyPermission() != null ? thread.getReplyPermission().name() : "ANYONE");
        
        // Timestamps
        dto.setCreatedAt(thread.getCreatedAt());
        dto.setUpdatedAt(thread.getUpdatedAt());
        dto.setEditedAt(thread.getEditedAt());
        
        // Media
        List<ThreadMediaDto> mediaDtos = thread.getMedia().stream()
            .map(this::convertMediaToDto)
            .collect(Collectors.toList());
        dto.setMedia(mediaDtos);
        
        // User engagement status
        if (currentUserId != null) {
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            if (currentUser != null) {
                dto.setIsLiked(threadLikeRepository.existsByThreadIdAndUserId(thread.getId(), currentUserId));
                dto.setIsReposted(threadRepostRepository.existsByThreadIdAndUserId(thread.getId(), currentUserId));
                dto.setIsBookmarked(threadBookmarkRepository.existsByThreadIdAndUserId(thread.getId(), currentUserId));
                
                Optional<ThreadReaction> userReaction = threadReactionRepository.findByThreadAndUser(thread, currentUser);
                dto.setUserReaction(userReaction.map(ThreadReaction::getReactionType).orElse(null));
            }
        }
        
        // Hashtags and mentions
        List<String> hashtags = thread.getHashtags().stream()
            .map(th -> th.getHashtag().getTag())
            .collect(Collectors.toList());
        dto.setHashtags(hashtags);
        
        List<String> mentions = thread.getMentions().stream()
            .map(tm -> tm.getMentionedUser().getUsername())
            .collect(Collectors.toList());
        dto.setMentions(mentions);
        
        // Quoted thread
        if (thread.getQuotedThread() != null) {
            dto.setQuotedThread(convertToDto(thread.getQuotedThread(), currentUserId));
        }
        
        return dto;
    }
    
    // Convert ThreadMedia entity to DTO
    private ThreadMediaDto convertMediaToDto(ThreadMedia media) {
        ThreadMediaDto dto = new ThreadMediaDto();
        dto.setId(media.getId());
        dto.setThreadId(media.getThread().getId());
        dto.setMediaType(media.getMediaType());
        dto.setMediaUrl(media.getMediaUrl());
        dto.setThumbnailUrl(media.getThumbnailUrl());
        dto.setAltText(media.getAltText());
        dto.setFileSize(media.getFileSize());
        dto.setDuration(media.getDuration());
        dto.setWidth(media.getWidth());
        dto.setHeight(media.getHeight());
        dto.setDisplayOrder(media.getDisplayOrder());
        dto.setCreatedAt(media.getCreatedAt());
        return dto;
    }
}
