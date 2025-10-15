package com.urutte.controller;

import com.urutte.dto.ThreadDto;
import com.urutte.exception.ThreadAccessDeniedException;
import com.urutte.model.ReactionType;
import com.urutte.model.User;
import com.urutte.service.MediaUploadService;
import com.urutte.service.ThreadService;
import com.urutte.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/threads")
@CrossOrigin(origins = {"http://localhost", "http://localhost:80", "http://frontend", "https://urutte.com", "https://www.urutte.com"})
public class ThreadController {
    
    @Autowired
    private ThreadService threadService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private MediaUploadService mediaUploadService;
    
    // Create a new thread
    @PostMapping
    public ResponseEntity<ThreadDto> createThread(
            @RequestParam("content") String content,
            @RequestParam(value = "parentThreadId", required = false) Long parentThreadId,
            @RequestParam(value = "media", required = false) MultipartFile media,
            @RequestParam(value = "replyPermission", required = false, defaultValue = "ANYONE") String replyPermission,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            String mediaUrl = null;
            String mediaType = null;
            
            if (media != null && !media.isEmpty()) {
                // Handle media upload
                mediaUrl = mediaUploadService.uploadMedia(media);
                mediaType = mediaUploadService.getMediaType(media);
            }
            
            ThreadDto thread = threadService.createThread(content, user.getId(), parentThreadId, mediaUrl, mediaType, replyPermission);
            return ResponseEntity.ok(thread);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // Create a new thread with multiple media files
    @PostMapping("/multiple-media")
    public ResponseEntity<ThreadDto> createThreadWithMultipleMedia(
            @RequestParam("content") String content,
            @RequestParam(value = "parentThreadId", required = false) Long parentThreadId,
            @RequestParam(value = "media", required = false) List<MultipartFile> mediaFiles,
            @RequestParam(value = "replyPermission", required = false, defaultValue = "ANYONE") String replyPermission,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            List<String> mediaUrls = null;
            List<String> mediaTypes = null;
            
            if (mediaFiles != null && !mediaFiles.isEmpty()) {
                mediaUrls = new java.util.ArrayList<>();
                mediaTypes = new java.util.ArrayList<>();
                
                for (MultipartFile media : mediaFiles) {
                    if (media != null && !media.isEmpty()) {
                        String mediaUrl = mediaUploadService.uploadMedia(media);
                        String mediaType = mediaUploadService.getMediaType(media);
                        mediaUrls.add(mediaUrl);
                        mediaTypes.add(mediaType);
                    }
                }
            }
            
            ThreadDto thread = threadService.createThreadWithMedia(content, user.getId(), parentThreadId, mediaUrls, mediaTypes, replyPermission);
            return ResponseEntity.ok(thread);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // Create a quote repost
    @PostMapping("/quote-repost")
    public ResponseEntity<ThreadDto> createQuoteRepost(
            @RequestParam("content") String content,
            @RequestParam("quotedThreadId") Long quotedThreadId,
            @RequestParam(value = "media", required = false) MultipartFile media,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            String mediaUrl = null;
            String mediaType = null;
            
            if (media != null && !media.isEmpty()) {
                // Handle media upload
                mediaUrl = mediaUploadService.uploadMedia(media);
                mediaType = mediaUploadService.getMediaType(media);
            }
            
            ThreadDto thread = threadService.createQuoteRepost(content, user.getId(), quotedThreadId, mediaUrl, mediaType);
            return ResponseEntity.ok(thread);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // Get main threads (feed)
    @GetMapping
    public ResponseEntity<Page<ThreadDto>> getMainThreads(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        String userId = user != null ? user.getId() : null;
        
        try {
            Page<ThreadDto> threads = threadService.getMainThreads(userId, page, size);
            return ResponseEntity.ok(threads);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // Get feed endpoint (alias for main threads)
    @GetMapping("/feed")
    public ResponseEntity<Page<ThreadDto>> getFeed(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        String userId = user != null ? user.getId() : null;
        
        try {
            Page<ThreadDto> threads = threadService.getMainThreads(userId, page, size);
            return ResponseEntity.ok(threads);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // Get thread by ID
    @GetMapping("/{threadId}")
    public ResponseEntity<?> getThreadById(
            @PathVariable Long threadId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        String userId = user != null ? user.getId() : null;
        
        try {
            ThreadDto thread = threadService.getThreadById(threadId, userId);
            return ResponseEntity.ok(thread);
        } catch (ThreadAccessDeniedException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "ACCESS_DENIED");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(403).body(errorResponse);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }
    
    // Get user's own threads
    @GetMapping("/my-threads")
    public ResponseEntity<Page<ThreadDto>> getMyThreads(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            Page<ThreadDto> threads = threadService.getUserThreads(user.getId(), page, size);
            return ResponseEntity.ok(threads);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // Edit a thread
    @PutMapping("/{threadId}")
    public ResponseEntity<ThreadDto> editThread(
            @PathVariable Long threadId,
            @RequestParam("content") String content,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            ThreadDto updatedThread = threadService.editThread(threadId, content, user.getId());
            return ResponseEntity.ok(updatedThread);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // Get replies for a thread
    @GetMapping("/{threadId}/replies")
    public ResponseEntity<List<ThreadDto>> getThreadReplies(
            @PathVariable Long threadId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        String userId = user != null ? user.getId() : null;
        
        try {
            List<ThreadDto> replies = threadService.getThreadReplies(threadId, userId);
            return ResponseEntity.ok(replies);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // Like a thread
    @PostMapping("/{threadId}/like")
    public ResponseEntity<Map<String, Object>> likeThread(
            @PathVariable Long threadId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            boolean isLiked = threadService.likeThread(threadId, user.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("isLiked", isLiked);
            response.put("message", isLiked ? "Thread liked" : "Thread unliked");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // Repost a thread
    @PostMapping("/{threadId}/repost")
    public ResponseEntity<Map<String, Object>> repostThread(
            @PathVariable Long threadId,
            @RequestParam(value = "quoteContent", required = false) String quoteContent,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            boolean isReposted = threadService.repostThread(threadId, user.getId(), quoteContent);
            Map<String, Object> response = new HashMap<>();
            response.put("isReposted", isReposted);
            response.put("message", isReposted ? "Thread reposted" : "Thread unreposted");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // Bookmark a thread
    @PostMapping("/{threadId}/bookmark")
    public ResponseEntity<Map<String, Object>> bookmarkThread(
            @PathVariable Long threadId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            boolean isBookmarked = threadService.bookmarkThread(threadId, user.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("isBookmarked", isBookmarked);
            response.put("message", isBookmarked ? "Thread bookmarked" : "Thread unbookmarked");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // Add reaction to thread
    @PostMapping("/{threadId}/reaction")
    public ResponseEntity<Map<String, Object>> addReaction(
            @PathVariable Long threadId,
            @RequestParam("reactionType") String reactionType,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            ReactionType type = ReactionType.valueOf(reactionType.toUpperCase());
            boolean hasReaction = threadService.addReaction(threadId, user.getId(), type);
            Map<String, Object> response = new HashMap<>();
            response.put("hasReaction", hasReaction);
            response.put("reactionType", reactionType);
            response.put("message", hasReaction ? "Reaction added" : "Reaction removed");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // Delete a thread
    @DeleteMapping("/{threadId}")
    public ResponseEntity<Map<String, String>> deleteThread(
            @PathVariable Long threadId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            boolean deleted = threadService.deleteThread(threadId, user.getId());
            Map<String, String> response = new HashMap<>();
            response.put("success", String.valueOf(deleted));
            response.put("message", deleted ? "Thread deleted successfully" : "Failed to delete thread");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // Search threads
    @GetMapping("/search")
    public ResponseEntity<Page<ThreadDto>> searchThreads(
            @RequestParam("q") String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        String userId = user != null ? user.getId() : null;
        
        try {
            Page<ThreadDto> threads = threadService.searchThreads(keyword, userId, page, size);
            return ResponseEntity.ok(threads);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // Get liked threads by current user
    @GetMapping("/liked")
    public ResponseEntity<List<ThreadDto>> getLikedThreads(
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            List<ThreadDto> likedThreads = threadService.getLikedThreadsByUser(user.getId(), limit);
            return ResponseEntity.ok(likedThreads);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // Get threads by hashtag
    @GetMapping("/hashtag/{hashtag}")
    public ResponseEntity<Page<ThreadDto>> getThreadsByHashtag(
            @PathVariable String hashtag,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        String userId = user != null ? user.getId() : null;
        
        try {
            Page<ThreadDto> threads = threadService.getThreadsByHashtag(hashtag, userId, page, size);
            return ResponseEntity.ok(threads);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // Get trending threads
    @GetMapping("/trending")
    public ResponseEntity<Page<ThreadDto>> getTrendingThreads(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        String userId = user != null ? user.getId() : null;
        
        try {
            Page<ThreadDto> threads = threadService.getTrendingThreads(userId, page, size);
            return ResponseEntity.ok(threads);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // Helper method to get current user
    private User getCurrentUser(String authHeader, OidcUser principal) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return userService.getUserFromToken(token);
        } else if (principal != null) {
            return userService.getOrCreateUser(principal);
        }
        return null;
    }
}
