package com.urutte.service;

import com.urutte.model.Follow;
import com.urutte.model.FollowRequest;
import com.urutte.model.User;
import com.urutte.repository.FollowRepository;
import com.urutte.repository.FollowRequestRepository;
import com.urutte.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AiFollowRequestService {

    private static final Logger logger = LoggerFactory.getLogger(AiFollowRequestService.class);

    @Autowired
    private FollowRequestRepository followRequestRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Auto-approve follow requests to AI users after 30 minutes
     * Runs every 5 minutes to check for requests that need auto-approval
     */
    @Scheduled(fixedRate = 300000) // 5 minutes = 300,000 milliseconds
    @Transactional
    public void autoApproveFollowRequestsToAiUser() {
        try {
            // Calculate cutoff time (30 minutes ago)
            Instant cutoffTime = Instant.now().minusSeconds(30 * 60); // 30 minutes

            // Find pending follow requests to AI user older than 30 minutes
            List<FollowRequest> pendingRequests = followRequestRepository
                .findPendingFollowRequestsToAiUserOlderThan(cutoffTime);

            if (pendingRequests.isEmpty()) {
                logger.debug("No pending follow requests to AI user found for auto-approval");
                return;
            }

            logger.info("Found {} pending follow requests to AI user for auto-approval", pendingRequests.size());

            int approvedCount = 0;
            for (FollowRequest followRequest : pendingRequests) {
                try {
                    // Check if already following (in case of race condition)
                    if (followRepository.existsByFollowerIdAndFollowingId(
                        followRequest.getRequester().getId(), 
                        followRequest.getTarget().getId())) {
                        
                        // Already following, just update the request status
                        followRequest.setStatus(FollowRequest.FollowRequestStatus.APPROVED);
                        followRequestRepository.save(followRequest);
                        logger.debug("Updated follow request status to APPROVED for user {} (already following AI user)", 
                            followRequest.getRequester().getUsername());
                    } else {
                        // Approve the follow request
                        followRequest.setStatus(FollowRequest.FollowRequestStatus.APPROVED);
                        followRequestRepository.save(followRequest);

                        // Create the follow relationship
                        Follow follow = new Follow(followRequest.getRequester(), followRequest.getTarget());
                        followRepository.save(follow);

                        // Create notification for the requester
                        notificationService.createNotification(
                            followRequest.getRequester().getId(),
                            followRequest.getTarget().getId(),
                            "follow_approved",
                            "Follow Request Auto-Approved",
                            "Your follow request to " + followRequest.getTarget().getName() + " has been automatically approved",
                            "user",
                            null
                        );

                        logger.info("Auto-approved follow request from user {} to AI user", 
                            followRequest.getRequester().getUsername());
                    }
                    
                    approvedCount++;
                } catch (Exception e) {
                    logger.error("Error auto-approving follow request {}: {}", 
                        followRequest.getId(), e.getMessage(), e);
                }
            }

            logger.info("Auto-approved {} follow requests to AI user", approvedCount);

        } catch (Exception e) {
            logger.error("Error in auto-approval of follow requests to AI user", e);
        }
    }

    /**
     * Manually trigger auto-approval (for testing or immediate processing)
     */
    @Transactional
    public void triggerAutoApproval() {
        logger.info("Manually triggering auto-approval of follow requests to AI user");
        autoApproveFollowRequestsToAiUser();
    }

    /**
     * Get count of pending follow requests to AI user
     */
    public long getPendingFollowRequestsToAiUserCount() {
        try {
            User aiUser = userRepository.findByEmail("ai@urutte.com")
                .orElse(null);
            
            if (aiUser == null) {
                return 0;
            }

            return followRequestRepository.countByTargetAndStatus(
                aiUser, FollowRequest.FollowRequestStatus.PENDING);
        } catch (Exception e) {
            logger.error("Error getting pending follow requests count to AI user", e);
            return 0;
        }
    }
}
