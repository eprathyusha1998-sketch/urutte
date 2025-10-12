package com.urutte.repository;

import com.urutte.model.FollowRequest;
import com.urutte.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRequestRepository extends JpaRepository<FollowRequest, Long> {
    
    // Check if a follow request exists between two users
    Optional<FollowRequest> findByRequesterAndTarget(User requester, User target);
    
    // Check if a follow request exists by user IDs
    boolean existsByRequesterIdAndTargetId(String requesterId, String targetId);
    
    // Get pending follow requests for a user (people who want to follow them)
    Page<FollowRequest> findByTargetAndStatusOrderByCreatedAtDesc(User target, FollowRequest.FollowRequestStatus status, Pageable pageable);
    
    // Get follow requests sent by a user
    Page<FollowRequest> findByRequesterAndStatusOrderByCreatedAtDesc(User requester, FollowRequest.FollowRequestStatus status, Pageable pageable);
    
    // Count pending follow requests for a user
    long countByTargetAndStatus(User target, FollowRequest.FollowRequestStatus status);
    
    // Get all pending follow requests for a user
    List<FollowRequest> findByTargetAndStatusOrderByCreatedAtDesc(User target, FollowRequest.FollowRequestStatus status);
    
    // Get all follow requests sent by a user
    List<FollowRequest> findByRequesterAndStatusOrderByCreatedAtDesc(User requester, FollowRequest.FollowRequestStatus status);
    
    // Delete follow request by requester and target
    void deleteByRequesterAndTarget(User requester, User target);
    
    // Get follow request by IDs
    @Query("SELECT fr FROM FollowRequest fr WHERE fr.requester.id = :requesterId AND fr.target.id = :targetId")
    Optional<FollowRequest> findByRequesterIdAndTargetId(@Param("requesterId") String requesterId, @Param("targetId") String targetId);
}
