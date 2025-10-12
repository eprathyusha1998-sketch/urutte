package com.urutte.repository;

import com.urutte.model.Follow;
import com.urutte.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    
    // Check if user follows another user
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    
    // Get all users that a user follows
    List<Follow> findByFollower(User follower);
    
    // Get all users that follow a user
    List<Follow> findByFollowing(User following);
    
    // Count followers
    long countByFollowing(User following);
    
    // Count following
    long countByFollower(User follower);
    
    // Check if user follows another user by IDs
    boolean existsByFollowerIdAndFollowingId(String followerId, String followingId);
    
    // Get followers list
    @Query("SELECT f.follower FROM Follow f WHERE f.following.id = :userId")
    List<User> findFollowersByUserId(@Param("userId") String userId);
    
    // Get following list
    @Query("SELECT f.following FROM Follow f WHERE f.follower.id = :userId")
    List<User> findFollowingByUserId(@Param("userId") String userId);
}
