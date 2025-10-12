package com.urutte.service;

import com.urutte.dto.UserDto;
import com.urutte.model.FollowRequest;
import com.urutte.model.User;
import com.urutte.repository.FollowRepository;
import com.urutte.repository.FollowRequestRepository;
import com.urutte.repository.PostRepository;
import com.urutte.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FollowRepository followRepository;
    
    @Autowired
    private FollowRequestRepository followRequestRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private ProfilePictureService profilePictureService;
    
    @Autowired
    private NotificationService notificationService;
    
    public User getOrCreateUser(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public User getUserById(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public User getUserFromToken(String token) {
        // For now, we'll use the token as the user ID
        // In production, you would validate the JWT token and extract the user ID
        // This is a simplified implementation for development
        try {
            return userRepository.findById(token)
                .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
    
    public User getOrCreateUser(OidcUser oidcUser) {
        String userId = oidcUser.getSubject();
        Optional<User> existingUser = userRepository.findById(userId);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Update user info from OAuth
            user.setName(oidcUser.getFullName());
            user.setEmail(oidcUser.getEmail());
            
            // Download and store profile picture if it's an external URL
            String pictureUrl = oidcUser.getPicture();
            if (pictureUrl != null && (pictureUrl.startsWith("http://") || pictureUrl.startsWith("https://"))) {
                try {
                    String localPicturePath = profilePictureService.downloadAndStoreProfilePicture(pictureUrl, userId);
                    if (localPicturePath != null) {
                        user.setPicture("/" + localPicturePath);
                    } else {
                        // Fallback to default avatar
                        user.setPicture(profilePictureService.generateDefaultAvatar(userId));
                    }
                } catch (Exception e) {
                    // Fallback to default avatar if download fails
                    user.setPicture(profilePictureService.generateDefaultAvatar(userId));
                }
            } else {
                user.setPicture(pictureUrl);
            }
            
            return userRepository.save(user);
        } else {
            // Create new user
            User newUser = new User();
            newUser.setId(userId);
            newUser.setName(oidcUser.getFullName());
            newUser.setEmail(oidcUser.getEmail());
            
            // Download and store profile picture if it's an external URL
            String pictureUrl = oidcUser.getPicture();
            if (pictureUrl != null && (pictureUrl.startsWith("http://") || pictureUrl.startsWith("https://"))) {
                try {
                    String localPicturePath = profilePictureService.downloadAndStoreProfilePicture(pictureUrl, userId);
                    if (localPicturePath != null) {
                        newUser.setPicture("/" + localPicturePath);
                    } else {
                        // Fallback to default avatar
                        newUser.setPicture(profilePictureService.generateDefaultAvatar(userId));
                    }
                } catch (Exception e) {
                    // Fallback to default avatar if download fails
                    newUser.setPicture(profilePictureService.generateDefaultAvatar(userId));
                }
            } else {
                newUser.setPicture(pictureUrl);
            }
            
            return userRepository.save(newUser);
        }
    }
    
    public UserDto getUserProfile(String userId, String currentUserId) {
        User user = getOrCreateUser(userId);
        return convertToDto(user, currentUserId);
    }
    
    public UserDto followUser(String userId, String currentUserId) {
        User userToFollow = getOrCreateUser(userId);
        User currentUser = getOrCreateUser(currentUserId);
        
        if (userId.equals(currentUserId)) {
            throw new RuntimeException("Cannot follow yourself");
        }
        
        // Check if already following
        if (followRepository.existsByFollowerIdAndFollowingId(currentUserId, userId)) {
            // Unfollow: remove the follow relationship
            followRepository.findByFollowerAndFollowing(currentUser, userToFollow)
                .ifPresent(followRepository::delete);
        } else {
            // Check if there's already a pending follow request
            if (followRequestRepository.existsByRequesterIdAndTargetId(currentUserId, userId)) {
                // Cancel the follow request
                followRequestRepository.findByRequesterAndTarget(currentUser, userToFollow)
                    .ifPresent(followRequestRepository::delete);
            } else {
                // Create a new follow request
                FollowRequest followRequest = new FollowRequest(currentUser, userToFollow);
                followRequestRepository.save(followRequest);
                
                // Create notification for the target user
                notificationService.createNotification(
                    userToFollow.getId(),
                    currentUser.getId(),
                    "follow_request",
                    "New Follow Request",
                    currentUser.getName() + " wants to follow you",
                    "user",
                    null // No relatedEntityId needed for user notifications since we have fromUser
                );
            }
        }
        
        return convertToDto(userToFollow, currentUserId);
    }
    
    public List<UserDto> getFollowers(String userId, String currentUserId) {
        List<User> followers = followRepository.findFollowersByUserId(userId);
        return followers.stream()
            .map(user -> convertToDto(user, currentUserId))
            .collect(java.util.stream.Collectors.toList());
    }
    
    public List<UserDto> getFollowing(String userId, String currentUserId) {
        List<User> following = followRepository.findFollowingByUserId(userId);
        return following.stream()
            .map(user -> convertToDto(user, currentUserId))
            .collect(java.util.stream.Collectors.toList());
    }
    
    public List<UserDto> searchUsers(String keyword, String currentUserId) {
        List<User> users = userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
        return users.stream()
            .map(user -> convertToDto(user, currentUserId))
            .collect(java.util.stream.Collectors.toList());
    }
    
    public List<UserDto> getPeopleYouMayKnow(String currentUserId, int limit) {
        // Get users that the current user doesn't follow and is not the current user
        List<User> allUsers = userRepository.findAll();
        List<User> following = followRepository.findFollowingByUserId(currentUserId);
        List<String> followingIds = following.stream().map(User::getId).collect(java.util.stream.Collectors.toList());
        
        List<User> suggestedUsers = allUsers.stream()
            .filter(user -> !user.getId().equals(currentUserId)) // Not current user
            .filter(user -> !followingIds.contains(user.getId())) // Not already following
            .limit(limit)
            .collect(java.util.stream.Collectors.toList());
        
        return suggestedUsers.stream()
            .map(user -> convertToDto(user, currentUserId))
            .collect(java.util.stream.Collectors.toList());
    }
    
    public List<FollowRequest> getPendingFollowRequests(String currentUserId) {
        User currentUser = getOrCreateUser(currentUserId);
        return followRequestRepository.findByTargetAndStatusOrderByCreatedAtDesc(
            currentUser, FollowRequest.FollowRequestStatus.PENDING);
    }
    
    public UserDto approveFollowRequest(Long followRequestId, String currentUserId) {
        FollowRequest followRequest = followRequestRepository.findById(followRequestId)
            .orElseThrow(() -> new RuntimeException("Follow request not found"));
        
        if (!followRequest.getTarget().getId().equals(currentUserId)) {
            throw new RuntimeException("You can only approve your own follow requests");
        }
        
        if (followRequest.getStatus() != FollowRequest.FollowRequestStatus.PENDING) {
            throw new RuntimeException("Follow request is not pending");
        }
        
        // Approve the follow request
        followRequest.setStatus(FollowRequest.FollowRequestStatus.APPROVED);
        followRequestRepository.save(followRequest);
        
        // Create the follow relationship
        com.urutte.model.Follow follow = new com.urutte.model.Follow(followRequest.getRequester(), followRequest.getTarget());
        followRepository.save(follow);
        
        // Create notification for the requester
        notificationService.createNotification(
            followRequest.getRequester().getId(),
            followRequest.getTarget().getId(),
            "follow_approved",
            "Follow Request Approved",
            followRequest.getTarget().getName() + " approved your follow request",
            "user",
            null // No relatedEntityId needed for user notifications since we have fromUser
        );
        
        return convertToDto(followRequest.getRequester(), currentUserId);
    }
    
    public UserDto rejectFollowRequest(Long followRequestId, String currentUserId) {
        FollowRequest followRequest = followRequestRepository.findById(followRequestId)
            .orElseThrow(() -> new RuntimeException("Follow request not found"));
        
        if (!followRequest.getTarget().getId().equals(currentUserId)) {
            throw new RuntimeException("You can only reject your own follow requests");
        }
        
        if (followRequest.getStatus() != FollowRequest.FollowRequestStatus.PENDING) {
            throw new RuntimeException("Follow request is not pending");
        }
        
        // Reject the follow request
        followRequest.setStatus(FollowRequest.FollowRequestStatus.REJECTED);
        followRequestRepository.save(followRequest);
        
        return convertToDto(followRequest.getRequester(), currentUserId);
    }
    
    public UserDto convertToDto(User user, String currentUserId) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPicture(user.getPicture());
        dto.setBio(user.getBio());
        dto.setLocation(user.getLocation());
        dto.setWebsite(user.getWebsite());
        
        // Set counts
        dto.setFollowersCount(followRepository.countByFollowing(user));
        dto.setFollowingCount(followRepository.countByFollower(user));
        dto.setPostsCount(postRepository.countByUser(user));
        
        // Set following status
        if (currentUserId != null && !currentUserId.equals(user.getId())) {
            dto.setFollowing(followRepository.existsByFollowerIdAndFollowingId(currentUserId, user.getId()));
        }
        
        return dto;
    }
}
