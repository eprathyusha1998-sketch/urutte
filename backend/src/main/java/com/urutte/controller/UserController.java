package com.urutte.controller;

import com.urutte.dto.UserDto;
import com.urutte.model.FollowRequest;
import com.urutte.model.User;
import com.urutte.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        // Try token-based auth first
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            User user = userService.getUserFromToken(token);
            if (user != null) {
                UserDto userDto = userService.getUserProfile(user.getId(), user.getId());
                return ResponseEntity.ok(userDto);
            }
        }
        
        // Fallback to OAuth2
        if (principal != null) {
            User user = userService.getOrCreateUser(principal);
            UserDto userDto = userService.getUserProfile(user.getId(), user.getId());
            return ResponseEntity.ok(userDto);
        }
        
        return ResponseEntity.status(401).build();
    }
    
    @PutMapping("/me")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String website,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String dateOfBirth,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Boolean isPrivate,
            @RequestParam(required = false) MultipartFile profileImage,
            @RequestParam(required = false) MultipartFile coverImage) {
        
        try {
            User currentUser = getCurrentUserFromAuth(authHeader, principal);
            if (currentUser == null) {
                return ResponseEntity.status(401).build();
            }
            
            UserDto updatedUser = userService.updateUserProfile(
                currentUser.getId(),
                name, username, bio, location, website, phoneNumber, 
                dateOfBirth, gender, isPrivate, profileImage, coverImage
            );
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Profile updated successfully",
                "user", updatedUser
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error updating profile: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "Failed to update profile: " + e.getMessage()
            );
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserProfile(
            @PathVariable String userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        try {
            User currentUser = getCurrentUserFromAuth(authHeader, principal);
            if (currentUser == null) {
                return ResponseEntity.status(401).build();
            }
            
            UserDto userDto = userService.getUserProfile(userId, currentUser.getId());
            return ResponseEntity.ok(userDto);
        } catch (Exception e) {
            System.err.println("Error getting user profile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    @PostMapping("/{userId}/follow")
    public ResponseEntity<UserDto> followUser(
            @PathVariable String userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        try {
            User currentUser = getCurrentUserFromAuth(authHeader, principal);
            if (currentUser == null) {
                return ResponseEntity.status(401).build();
            }
            
            UserDto userDto = userService.followUser(userId, currentUser.getId());
            return ResponseEntity.ok(userDto);
        } catch (Exception e) {
            System.err.println("Error following user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<UserDto>> getFollowers(
            @PathVariable String userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        try {
            User currentUser = getCurrentUserFromAuth(authHeader, principal);
            if (currentUser == null) {
                return ResponseEntity.status(401).build();
            }
            
            List<UserDto> followers = userService.getFollowers(userId, currentUser.getId());
            return ResponseEntity.ok(followers);
        } catch (Exception e) {
            System.err.println("Error getting followers: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/{userId}/following")
    public ResponseEntity<List<UserDto>> getFollowing(
            @PathVariable String userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        try {
            User currentUser = getCurrentUserFromAuth(authHeader, principal);
            if (currentUser == null) {
                return ResponseEntity.status(401).build();
            }
            
            List<UserDto> following = userService.getFollowing(userId, currentUser.getId());
            return ResponseEntity.ok(following);
        } catch (Exception e) {
            System.err.println("Error getting following: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchUsers(
            @RequestParam String q,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        try {
            User currentUser = getCurrentUserFromAuth(authHeader, principal);
            if (currentUser == null) {
                return ResponseEntity.status(401).build();
            }
            
            List<UserDto> users = userService.searchUsers(q, currentUser.getId());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            System.err.println("Error searching users: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/suggestions")
    public ResponseEntity<List<UserDto>> getPeopleYouMayKnow(
            @RequestParam(defaultValue = "5") int limit,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        try {
            User currentUser = getCurrentUserFromAuth(authHeader, principal);
            if (currentUser == null) {
                return ResponseEntity.status(401).build();
            }
            
            List<UserDto> suggestions = userService.getPeopleYouMayKnow(currentUser.getId(), limit);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            System.err.println("Error getting people suggestions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    // Helper method to get user from either token or OAuth
    private User getCurrentUserFromAuth(String authHeader, OidcUser principal) {
        // Try token-based auth first
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return userService.getUserFromToken(token);
        }
        
        // Fallback to OAuth2
        if (principal != null) {
            return userService.getOrCreateUser(principal);
        }
        
        return null;
    }
    
    @GetMapping("/follow-requests")
    public ResponseEntity<List<FollowRequest>> getPendingFollowRequests(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        try {
            User currentUser = getCurrentUserFromAuth(authHeader, principal);
            if (currentUser == null) {
                return ResponseEntity.status(401).build();
            }
            
            List<FollowRequest> followRequests = userService.getPendingFollowRequests(currentUser.getId());
            return ResponseEntity.ok(followRequests);
        } catch (Exception e) {
            System.err.println("Error getting follow requests: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/follow-requests/pending")
    public ResponseEntity<List<FollowRequest>> getPendingFollowRequestsEndpoint(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        try {
            User currentUser = getCurrentUserFromAuth(authHeader, principal);
            if (currentUser == null) {
                return ResponseEntity.status(401).build();
            }
            
            List<FollowRequest> followRequests = userService.getPendingFollowRequests(currentUser.getId());
            return ResponseEntity.ok(followRequests);
        } catch (Exception e) {
            System.err.println("Error getting pending follow requests: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/{userId}/follow-request")
    public ResponseEntity<Map<String, Object>> sendFollowRequest(
            @PathVariable String userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        try {
            User currentUser = getCurrentUserFromAuth(authHeader, principal);
            if (currentUser == null) {
                return ResponseEntity.status(401).build();
            }
            
            if (userId.equals(currentUser.getId())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Cannot send follow request to yourself"
                ));
            }
            
            FollowRequest followRequest = userService.sendFollowRequest(currentUser.getId(), userId);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Follow request sent successfully",
                "followRequest", followRequest
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error sending follow request: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "Failed to send follow request: " + e.getMessage()
            );
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PostMapping("/follow-requests/{followRequestId}/approve")
    public ResponseEntity<UserDto> approveFollowRequest(
            @PathVariable Long followRequestId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        try {
            User currentUser = getCurrentUserFromAuth(authHeader, principal);
            if (currentUser == null) {
                return ResponseEntity.status(401).build();
            }
            
            UserDto userDto = userService.approveFollowRequest(followRequestId, currentUser.getId());
            return ResponseEntity.ok(userDto);
        } catch (Exception e) {
            System.err.println("Error approving follow request: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    @PostMapping("/follow-requests/{followRequestId}/reject")
    public ResponseEntity<UserDto> rejectFollowRequest(
            @PathVariable Long followRequestId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal OidcUser principal) {
        
        try {
            User currentUser = getCurrentUserFromAuth(authHeader, principal);
            if (currentUser == null) {
                return ResponseEntity.status(401).build();
            }
            
            UserDto userDto = userService.rejectFollowRequest(followRequestId, currentUser.getId());
            return ResponseEntity.ok(userDto);
        } catch (Exception e) {
            System.err.println("Error rejecting follow request: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
