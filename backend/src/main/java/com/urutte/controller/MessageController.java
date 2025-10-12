package com.urutte.controller;

import com.urutte.dto.MessageDto;
import com.urutte.dto.UserDto;
import com.urutte.model.Message;
import com.urutte.model.User;
import com.urutte.service.MessageService;
import com.urutte.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

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

    @GetMapping("/conversation/{userId}")
    public ResponseEntity<List<MessageDto>> getConversation(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User currentUser = getCurrentUser(authHeader, principal);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        List<MessageDto> messages = messageService.getConversation(currentUser.getId(), userId, page, size);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Test endpoint working");
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<UserDto>> getConversationPartners() {
        
        // TEMPORARY FIX: Return empty list to avoid Hibernate ClassCastException
        // ISSUE: Hibernate ClassCastException when loading User entity
        // ROOT CAUSE: Conflicting entity mappings between old and new schema
        // TODO: Fix the Hibernate mapping issue by cleaning up entity relationships
        List<UserDto> partners = new ArrayList<>();
        return ResponseEntity.ok(partners);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<MessageDto>> getUnreadMessages(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        List<MessageDto> messages = messageService.getUnreadMessages(user.getId());
        return ResponseEntity.ok(messages);
    }

    @GetMapping({"/unread/count", "/unread-count"})
    public ResponseEntity<Map<String, Long>> getUnreadMessageCount(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        long count = messageService.getUnreadMessageCount(user.getId());
        
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/mark-read/{senderId}")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable String senderId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        messageService.markMessagesAsRead(user.getId(), senderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<MessageDto> sendMessage(
            @RequestBody SendMessageRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        MessageDto message = messageService.sendMessage(
            user.getId(), 
            request.getReceiverId(), 
            request.getContent(), 
            request.getMessageType(), 
            request.getMediaUrl()
        );
        return ResponseEntity.ok(message);
    }

    // Request DTO for sending messages
    public static class SendMessageRequest {
        private String receiverId;
        private String content;
        private String messageType = "text";
        private String mediaUrl;

        // Getters and setters
        public String getReceiverId() { return receiverId; }
        public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getMessageType() { return messageType; }
        public void setMessageType(String messageType) { this.messageType = messageType; }

        public String getMediaUrl() { return mediaUrl; }
        public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    }

}
