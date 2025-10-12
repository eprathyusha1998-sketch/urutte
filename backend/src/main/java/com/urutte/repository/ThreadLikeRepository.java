package com.urutte.repository;

import com.urutte.model.Thread;
import com.urutte.model.ThreadLike;
import com.urutte.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThreadLikeRepository extends JpaRepository<ThreadLike, Long> {
    
    // Check if user liked a thread
    Optional<ThreadLike> findByThreadAndUser(Thread thread, User user);
    
    // Count likes for a thread
    long countByThread(Thread thread);
    
    // Delete like by user and thread
    void deleteByThreadAndUser(Thread thread, User user);
    
    // Delete like by user and thread IDs
    void deleteByThreadIdAndUserId(Long threadId, String userId);
    
    // Check if user liked a thread by IDs
    boolean existsByThreadIdAndUserId(Long threadId, String userId);
    
    // Find all likes for a thread
    List<ThreadLike> findByThreadOrderByCreatedAtDesc(Thread thread);
    
    // Find all likes by a user
    List<ThreadLike> findByUserOrderByCreatedAtDesc(User user);
    
    // Find recent likes
    @Query("SELECT tl FROM ThreadLike tl ORDER BY tl.createdAt DESC")
    List<ThreadLike> findRecentLikes();
    
    // Find likes by date range
    @Query("SELECT tl FROM ThreadLike tl WHERE tl.createdAt BETWEEN :startDate AND :endDate ORDER BY tl.createdAt DESC")
    List<ThreadLike> findByDateRange(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find most liked threads
    @Query("SELECT tl.thread, COUNT(tl) as likeCount FROM ThreadLike tl GROUP BY tl.thread ORDER BY likeCount DESC")
    List<Object[]> findMostLikedThreads();
    
    // Find users who liked a thread
    @Query("SELECT tl.user FROM ThreadLike tl WHERE tl.thread = :thread ORDER BY tl.createdAt DESC")
    List<User> findUsersWhoLikedThread(@Param("thread") Thread thread);
    
    // Find threads liked by user
    @Query("SELECT tl.thread FROM ThreadLike tl WHERE tl.user = :user ORDER BY tl.createdAt DESC")
    List<Thread> findThreadsLikedByUser(@Param("user") User user);
}
