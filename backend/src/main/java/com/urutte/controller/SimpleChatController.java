package com.urutte.controller;

import com.urutte.dto.MessageDto;
import com.urutte.dto.UserDto;
import com.urutte.model.Message;
import com.urutte.model.User;
import com.urutte.service.MessageService;
import com.urutte.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/chat")
public class SimpleChatController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    // Store active SSE connections for each user
    private final Map<String, List<SseEmitter>> userConnections = new ConcurrentHashMap<>();

    // Helper method to get user from auth
    private User getCurrentUser(String authHeader, OidcUser principal) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return userService.getUserFromToken(token);
        } else if (principal != null) {
            return userService.getOrCreateUser(principal);
        }
        return null;
    }

    // Send message via REST API
    @PostMapping("/send")
    public ResponseEntity<MessageDto> sendMessage(
            @RequestBody SendMessageRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User sender = getCurrentUser(authHeader, principal);
        if (sender == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            User receiver = userService.getUserById(request.getReceiverId());
            if (receiver == null) {
                return ResponseEntity.status(404).body(null);
            }

            MessageDto message = messageService.sendMessage(
                sender.getId(), 
                receiver.getId(), 
                request.getContent(), 
                request.getMessageType(), 
                request.getMediaUrl()
            );

            // Send real-time notification via SSE
            sendSSENotification(receiver.getId(), message);

            return ResponseEntity.ok(message);
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // Get conversation messages
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

    // Get conversation partners
    @GetMapping("/conversations")
    public ResponseEntity<List<UserDto>> getConversationPartners(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        List<UserDto> partners = messageService.getConversationPartners(user.getId());
        return ResponseEntity.ok(partners);
    }

    // SSE endpoint for real-time notifications
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMessages(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return null;
        }

        SseEmitter emitter = new SseEmitter(30000L); // 30 second timeout
        
        // Add this connection to the user's connections
        userConnections.computeIfAbsent(user.getId(), k -> new CopyOnWriteArrayList<>()).add(emitter);
        
        // Cleanup on completion or error
        emitter.onCompletion(() -> removeConnection(user.getId(), emitter));
        emitter.onError(throwable -> removeConnection(user.getId(), emitter));
        emitter.onTimeout(() -> removeConnection(user.getId(), emitter));

        try {
            emitter.send(SseEmitter.event()
                .name("connected")
                .data("Connected to chat stream"));
        } catch (IOException e) {
            removeConnection(user.getId(), emitter);
        }

        return emitter;
    }

    // Get unread message count
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
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

    // Mark messages as read
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

    // Helper method to send SSE notification
    private void sendSSENotification(String userId, MessageDto message) {
        List<SseEmitter> connections = userConnections.get(userId);
        if (connections != null) {
            connections.removeIf(emitter -> {
                try {
                    emitter.send(SseEmitter.event()
                        .name("message")
                        .data(message));
                    return false;
                } catch (IOException e) {
                    return true; // Remove failed connection
                }
            });
        }
    }

    // Helper method to remove connection
    private void removeConnection(String userId, SseEmitter emitter) {
        List<SseEmitter> connections = userConnections.get(userId);
        if (connections != null) {
            connections.remove(emitter);
            if (connections.isEmpty()) {
                userConnections.remove(userId);
            }
        }
    }

    // Request DTO
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

