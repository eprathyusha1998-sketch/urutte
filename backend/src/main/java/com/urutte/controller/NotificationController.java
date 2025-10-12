package com.urutte.controller;

import com.urutte.dto.NotificationDto;
import com.urutte.model.User;
import com.urutte.service.NotificationService;
import com.urutte.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

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

    @GetMapping
    public ResponseEntity<Page<NotificationDto>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        Page<NotificationDto> notifications = notificationService.getUserNotifications(user.getId(), page, size);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        List<NotificationDto> notifications = notificationService.getUnreadNotifications(user.getId());
        return ResponseEntity.ok(notifications);
    }

    // Support both /unread/count and /unread-count endpoints
    @GetMapping({"/unread/count", "/unread-count"})
    public ResponseEntity<Map<String, Long>> getUnreadNotificationCount(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        long count = notificationService.getUnreadNotificationCount(user.getId());
        
        // Return as JSON object for consistency
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationDto> markAsRead(
            @PathVariable Long notificationId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        NotificationDto notification = notificationService.markAsRead(notificationId, user.getId());
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        User user = getCurrentUser(authHeader, principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok().build();
    }
}
