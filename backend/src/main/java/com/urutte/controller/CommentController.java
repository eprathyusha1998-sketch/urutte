package com.urutte.controller;

import com.urutte.dto.CommentDto;
import com.urutte.model.User;
import com.urutte.service.CommentService;
import com.urutte.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class CommentController {
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private UserService userService;
    
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
    
    @GetMapping("/{postId}/comments")
    public ResponseEntity<Page<CommentDto>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        Page<CommentDto> comments = commentService.getComments(postId, page, size, user.getId());
        return ResponseEntity.ok(comments);
    }
    
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable Long postId,
            @RequestBody CreateCommentRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        CommentDto comment = commentService.createComment(postId, request.getContent(), user.getId());
        return ResponseEntity.ok(comment);
    }
    
    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<CommentDto> createReply(
            @PathVariable Long commentId,
            @RequestBody CreateCommentRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        try {
            User user = getCurrentUser(authHeader, principal);
            if (user == null) {
                return ResponseEntity.status(401).build();
            }
            
            CommentDto reply = commentService.createReply(commentId, request.getContent(), user.getId());
            return ResponseEntity.ok(reply);
        } catch (Exception e) {
            System.err.println("Error creating reply: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<List<CommentDto>> getReplies(
            @PathVariable Long commentId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        List<CommentDto> replies = commentService.getReplies(commentId, user.getId());
        return ResponseEntity.ok(replies);
    }
    
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Map<String, Object>> deleteComment(
            @PathVariable Long commentId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        try {
            User user = getCurrentUser(authHeader, principal);
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "error", "Unauthorized"));
            }
            
            boolean deleted = commentService.deleteComment(commentId, user.getId());
            if (deleted) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Comment deleted successfully"));
            } else {
                return ResponseEntity.status(403).body(Map.of("success", false, "error", "You can only delete your own comments"));
            }
        } catch (Exception e) {
            System.err.println("Error deleting comment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "error", "Failed to delete comment: " + e.getMessage()));
        }
    }
    
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<CommentDto> likeComment(
            @PathVariable Long commentId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        try {
            User user = getCurrentUser(authHeader, principal);
            if (user == null) {
                return ResponseEntity.status(401).build();
            }
            
            CommentDto comment = commentService.likeComment(commentId, user.getId());
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            System.err.println("Error liking comment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    // Request DTO for creating comments
    public static class CreateCommentRequest {
        private String content;
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
