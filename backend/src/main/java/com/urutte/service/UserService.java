package com.urutte.service;

import com.urutte.dto.UserDto;
import com.urutte.model.FollowRequest;
import com.urutte.model.User;
import com.urutte.repository.FollowRepository;
import com.urutte.repository.FollowRequestRepository;
import com.urutte.repository.PostRepository;
import com.urutte.repository.UserRepository;
import com.urutte.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public User getOrCreateUser(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public User getUserById(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public User getUserFromToken(String token) {
        try {
            if (jwtUtil.validateToken(token)) {
                String userId = jwtUtil.extractUserId(token);
                return userRepository.findById(userId).orElse(null);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    public UserDto updateUserProfile(String userId, String name, String username, String bio, 
                                   String location, String website, String phoneNumber, 
                                   String dateOfBirth, String gender, Boolean isPrivate,
                                   MultipartFile profileImage, MultipartFile coverImage) throws IOException {
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update basic fields
        if (name != null && !name.trim().isEmpty()) {
            user.setName(name.trim());
        }
        if (username != null && !username.trim().isEmpty()) {
            user.setUsername(username.trim());
        }
        if (bio != null) {
            user.setBio(bio.trim());
        }
        if (location != null) {
            user.setLocation(location.trim());
        }
        if (website != null) {
            user.setWebsite(website.trim());
        }
        if (phoneNumber != null) {
            user.setPhoneNumber(phoneNumber.trim());
        }
        if (dateOfBirth != null) {
            user.setDateOfBirth(dateOfBirth.trim());
        }
        if (gender != null) {
            user.setGender(gender.trim());
        }
        if (isPrivate != null) {
            user.setIsPrivate(isPrivate);
        }
        
        // Handle profile image upload
        if (profileImage != null && !profileImage.isEmpty()) {
            String profileImagePath = profilePictureService.uploadProfileImage(profileImage, userId);
            user.setPicture("/" + profileImagePath);
        }
        
        // Handle cover image upload
        if (coverImage != null && !coverImage.isEmpty()) {
            String coverImagePath = profilePictureService.uploadCoverImage(coverImage, userId);
            user.setCoverPhoto("/" + coverImagePath);
        }
        
        user.setUpdatedAt(java.time.Instant.now());
        user = userRepository.save(user);
        
        return convertToDto(user, userId);
    }
    
    public User getOrCreateUser(OidcUser oidcUser) {
        String userId = oidcUser.getSubject();
        Optional<User> existingUser = userRepository.findById(userId);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Update user info from OAuth
            user.setName(oidcUser.getFullName());
            user.setEmail(oidcUser.getEmail());
            
            // Use default avatar instead of downloading Google profile picture
            user.setPicture(profilePictureService.generateDefaultAvatar(userId));
            
            return userRepository.save(user);
        } else {
            // Create new user
            User newUser = new User();
            newUser.setId(userId);
            newUser.setName(oidcUser.getFullName());
            newUser.setEmail(oidcUser.getEmail());
            
            // Use default avatar instead of downloading Google profile picture
            newUser.setPicture(profilePictureService.generateDefaultAvatar(userId));
            
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
    
    public UserDto unfollowUser(String userId, String currentUserId) {
        User userToUnfollow = getOrCreateUser(userId);
        User currentUser = getOrCreateUser(currentUserId);
        
        if (userId.equals(currentUserId)) {
            throw new RuntimeException("Cannot unfollow yourself");
        }
        
        // Check if currently following
        if (!followRepository.existsByFollowerIdAndFollowingId(currentUserId, userId)) {
            throw new RuntimeException("You are not following this user");
        }
        
        // Remove the follow relationship
        followRepository.findByFollowerAndFollowing(currentUser, userToUnfollow)
            .ifPresent(followRepository::delete);
        
        return convertToDto(userToUnfollow, currentUserId);
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
        
        // Get users who have been rejected within the last 3 months
        java.time.Instant threeMonthsAgo = java.time.Instant.now().minus(90, java.time.temporal.ChronoUnit.DAYS);
        List<FollowRequest> recentRejections = followRequestRepository.findByRequesterIdAndStatusAndUpdatedAtAfter(
            currentUserId, FollowRequest.FollowRequestStatus.REJECTED, threeMonthsAgo);
        List<String> recentlyRejectedIds = recentRejections.stream()
            .map(req -> req.getTarget().getId())
            .collect(java.util.stream.Collectors.toList());
        
        // Get users who have pending follow requests from current user
        List<FollowRequest> pendingRequests = followRequestRepository.findByRequesterIdAndStatusAndUpdatedAtAfter(
            currentUserId, FollowRequest.FollowRequestStatus.PENDING, java.time.Instant.EPOCH);
        List<String> pendingRequestIds = pendingRequests.stream()
            .map(req -> req.getTarget().getId())
            .collect(java.util.stream.Collectors.toList());
        
        List<User> suggestedUsers = allUsers.stream()
            .filter(user -> !user.getId().equals(currentUserId)) // Not current user
            .filter(user -> !followingIds.contains(user.getId())) // Not already following
            .filter(user -> !recentlyRejectedIds.contains(user.getId())) // Not recently rejected
            .filter(user -> !pendingRequestIds.contains(user.getId())) // Not already have pending request
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
    
    public FollowRequest sendFollowRequest(String requesterId, String targetId) {
        User requester = getOrCreateUser(requesterId);
        User target = getOrCreateUser(targetId);
        
        // Check if already following
        if (followRepository.existsByFollowerIdAndFollowingId(requesterId, targetId)) {
            throw new RuntimeException("Already following this user");
        }
        
        // Check if follow request already exists
        Optional<FollowRequest> existingRequest = followRequestRepository.findByRequesterAndTarget(requester, target);
        if (existingRequest.isPresent()) {
            FollowRequest request = existingRequest.get();
            if (request.getStatus() == FollowRequest.FollowRequestStatus.PENDING) {
                throw new RuntimeException("Follow request already pending");
            } else if (request.getStatus() == FollowRequest.FollowRequestStatus.APPROVED) {
                throw new RuntimeException("Already following this user");
            }
        }
        
        // Create new follow request
        FollowRequest followRequest = new FollowRequest();
        followRequest.setRequester(requester);
        followRequest.setTarget(target);
        followRequest.setStatus(FollowRequest.FollowRequestStatus.PENDING);
        followRequest.setCreatedAt(java.time.Instant.now());
        followRequest.setUpdatedAt(java.time.Instant.now());
        
        return followRequestRepository.save(followRequest);
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
        dto.setUsername(user.getUsername());
        dto.setPicture(user.getPicture());
        dto.setCoverPhoto(user.getCoverPhoto());
        dto.setBio(user.getBio());
        dto.setLocation(user.getLocation());
        dto.setWebsite(user.getWebsite());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setGender(user.getGender());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setVerified(user.getIsVerified());
        dto.setPrivate(user.getIsPrivate());
        dto.setActive(user.getIsActive());
        
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
