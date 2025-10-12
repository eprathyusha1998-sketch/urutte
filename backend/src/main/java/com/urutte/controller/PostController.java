package com.urutte.controller;

import com.urutte.dto.CreatePostDto;
import com.urutte.dto.PostDto;
import com.urutte.dto.QuoteRepostRequest;
import com.urutte.model.User;
import com.urutte.service.PostService;
import com.urutte.service.UserService;
import com.urutte.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FileUploadService fileUploadService;
    
    // Helper method to get user from either token or OAuth
    private User getCurrentUser(String authHeader, OidcUser principal) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return userService.getUserFromToken(token);
        } else if (principal != null) {
            return userService.getOrCreateUser(principal);
        }
        return null;
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        System.out.println("=== TEST ENDPOINT CALLED ===");
        return ResponseEntity.ok("CORS is working!");
    }

    @PostMapping
    public ResponseEntity<PostDto> createPost(
            @RequestBody CreatePostDto createPostDto,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        System.out.println("=== POST REQUEST RECEIVED ===");
        System.out.println("Content: " + createPostDto.getContent());
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            System.out.println("ERROR: No authentication found!");
            return ResponseEntity.status(401).build();
        }
        
        System.out.println("User: " + user.getName() + " (" + user.getId() + ")");
        PostDto post = postService.createPost(createPostDto, user.getId());
        System.out.println("Created post with ID: " + post.getId());
        System.out.println("=== POST REQUEST COMPLETED ===");
        
        return ResponseEntity.ok(post);
    }
    
    @PostMapping("/with-media")
    public ResponseEntity<Map<String, Object>> createPostWithMedia(
            @RequestParam("content") String content,
            @RequestParam(value = "media", required = false) MultipartFile media,
            @RequestParam(value = "parentPostId", required = false) Long parentPostId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String mediaUrl = null;
            String mediaType = null;
            
            if (media != null && !media.isEmpty()) {
                // Check file size (3MB max)
                if (media.getSize() > 3 * 1024 * 1024) {
                    response.put("success", false);
                    response.put("error", "File size must be less than 3MB");
                    return ResponseEntity.badRequest().body(response);
                }
                
                // Determine media type
                String contentType = media.getContentType();
                if (contentType != null) {
                    if (contentType.startsWith("image/")) {
                        mediaUrl = fileUploadService.uploadImage(media);
                        mediaType = "image";
                    } else if (contentType.startsWith("video/")) {
                        mediaUrl = fileUploadService.uploadVideo(media);
                        mediaType = "video";
                    } else {
                        response.put("success", false);
                        response.put("error", "Unsupported file type. Only images and videos are allowed.");
                        return ResponseEntity.badRequest().body(response);
                    }
                }
            }
            
            CreatePostDto createPostDto = new CreatePostDto();
            createPostDto.setContent(content);
            createPostDto.setMediaUrl(mediaUrl);
            createPostDto.setMediaType(mediaType);
            createPostDto.setParentPostId(parentPostId);
            
            PostDto post = postService.createPost(createPostDto, user.getId());
            
            response.put("success", true);
            response.put("post", post);
            response.put("message", "Post created successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("error", "Failed to upload media");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<Page<PostDto>> getAllPosts(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @AuthenticationPrincipal OidcUser principal) {
        User user = userService.getOrCreateUser(principal);
        Page<PostDto> posts = postService.getAllPosts(user.getId(), page, size);
        return ResponseEntity.ok(posts);
    }
    
    @GetMapping("/feed")
    public ResponseEntity<Page<PostDto>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        Page<PostDto> posts = postService.getFeed(user.getId(), page, size);
        return ResponseEntity.ok(posts);
    }
    
    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPostById(
            @PathVariable Long postId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        PostDto post = postService.getPostById(postId, user.getId());
        return ResponseEntity.ok(post);
    }
    
    @GetMapping("/{postId}/replies")
    public ResponseEntity<List<PostDto>> getReplies(@PathVariable Long postId,
                                                  @RequestHeader(value = "Authorization", required = false) String authHeader,
                                                  @AuthenticationPrincipal OidcUser principal) {
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        List<PostDto> replies = postService.getReplies(postId, user.getId());
        return ResponseEntity.ok(replies);
    }
    
    @PostMapping("/{postId}/like")
    public ResponseEntity<PostDto> likePost(
            @PathVariable Long postId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        PostDto post = postService.likePost(postId, user.getId());
        return ResponseEntity.ok(post);
    }
    
    @PostMapping("/{postId}/repost")
    public ResponseEntity<PostDto> repost(@PathVariable Long postId,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader,
                                        @AuthenticationPrincipal OidcUser principal) {
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        PostDto post = postService.repost(postId, user.getId());
        return ResponseEntity.ok(post);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<PostDto>> searchPosts(@RequestParam String q,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @AuthenticationPrincipal OidcUser principal) {
        User user = userService.getOrCreateUser(principal);
        Page<PostDto> posts = postService.searchPosts(q, user.getId(), page, size);
        return ResponseEntity.ok(posts);
    }
    
    @DeleteMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> deletePost(@PathVariable Long postId,
                                                         @RequestHeader(value = "Authorization", required = false) String authHeader,
                                                         @AuthenticationPrincipal OidcUser principal) {
        try {
            User currentUser = getCurrentUser(authHeader, principal);
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "error", "Unauthorized"));
            }
            
            boolean deleted = postService.deletePost(postId, currentUser.getId());
            if (deleted) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Post deleted successfully"));
            } else {
                return ResponseEntity.status(403).body(Map.of("success", false, "error", "You can only delete your own posts"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", "Failed to delete post"));
        }
    }
    
    @PostMapping("/quote-repost")
    public ResponseEntity<PostDto> createQuoteRepost(
            @RequestBody QuoteRepostRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        PostDto post = postService.createQuoteRepost(
            request.getContent(),
            request.getQuotedPostId(),
            request.getMediaUrl(),
            request.getMediaType(),
            user.getId()
        );
        
        return ResponseEntity.ok(post);
    }
    
    @PostMapping("/migrate-thread-hierarchy")
    public ResponseEntity<Map<String, String>> migrateThreadHierarchy(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            postService.migrateThreadHierarchy();
            return ResponseEntity.ok(Map.of("success", "true", "message", "Thread hierarchy migration completed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", "false", "error", "Migration failed: " + e.getMessage()));
        }
    }
}
