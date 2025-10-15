package com.urutte.repository;

import com.urutte.model.Thread;
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
public interface ThreadRepository extends JpaRepository<Thread, Long> {
    
    // Find threads by user
    Page<Thread> findByUserAndIsDeletedFalseOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Find main threads (not replies)
    Page<Thread> findByParentThreadIsNullAndIsDeletedFalseAndIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);
    
    // Find replies to a specific thread
    List<Thread> findByParentThreadAndIsDeletedFalseOrderByCreatedAtAsc(Thread parentThread);
    
    // Find replies to a specific thread by parent thread ID
    @Query("SELECT t FROM Thread t WHERE t.parentThread.id = :parentThreadId AND t.isDeleted = false ORDER BY t.createdAt ASC")
    List<Thread> findByParentThreadIdAndIsDeletedFalseOrderByCreatedAtAsc(@Param("parentThreadId") Long parentThreadId);
    
    // Find all replies in a thread hierarchy
    List<Thread> findByRootThreadAndIsDeletedFalseOrderByThreadPathAsc(Thread rootThread);
    
    // Find threads by hashtag
    @Query("SELECT t FROM Thread t JOIN t.hashtags th JOIN th.hashtag h WHERE h.tag = :hashtag AND t.isDeleted = false AND t.isPublic = true ORDER BY t.createdAt DESC")
    Page<Thread> findByHashtag(@Param("hashtag") String hashtag, Pageable pageable);
    
    // Find threads mentioning a user
    @Query("SELECT t FROM Thread t JOIN t.mentions tm WHERE tm.mentionedUser = :user AND t.isDeleted = false AND t.isPublic = true ORDER BY t.createdAt DESC")
    Page<Thread> findMentionsByUser(@Param("user") User user, Pageable pageable);
    
    // Find threads by content search
    @Query("SELECT t FROM Thread t WHERE t.content LIKE %:keyword% AND t.isDeleted = false AND t.isPublic = true ORDER BY t.createdAt DESC")
    Page<Thread> findByContentContaining(@Param("keyword") String keyword, Pageable pageable);
    
    // Find threads by user and content
    @Query("SELECT t FROM Thread t WHERE t.user = :user AND t.content LIKE %:keyword% AND t.isDeleted = false ORDER BY t.createdAt DESC")
    Page<Thread> findByUserAndContentContaining(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);
    
    // Find threads by thread type
    Page<Thread> findByThreadTypeAndIsDeletedFalseAndIsPublicTrueOrderByCreatedAtDesc(com.urutte.model.ThreadType threadType, Pageable pageable);
    
    // Find quoted threads
    @Query("SELECT t FROM Thread t WHERE t.quotedThread = :quotedThread AND t.isDeleted = false AND t.isPublic = true ORDER BY t.createdAt DESC")
    Page<Thread> findByQuotedThread(@Param("quotedThread") Thread quotedThread, Pageable pageable);
    
    // Find threads by thread level
    List<Thread> findByThreadLevelAndIsDeletedFalseOrderByCreatedAtAsc(Integer threadLevel);
    
    // Find threads by thread path
    List<Thread> findByThreadPathStartingWithAndIsDeletedFalseOrderByThreadPathAsc(String threadPath);
    
    // Count threads by user
    long countByUserAndIsDeletedFalse(User user);
    
    // Count replies to a thread
    long countByParentThreadAndIsDeletedFalse(Thread parentThread);
    
    // Find threads with media
    @Query("SELECT t FROM Thread t WHERE t.media IS NOT EMPTY AND t.isDeleted = false AND t.isPublic = true ORDER BY t.createdAt DESC")
    Page<Thread> findThreadsWithMedia(Pageable pageable);
    
    // Find pinned threads by user
    List<Thread> findByUserAndIsPinnedTrueAndIsDeletedFalseOrderByCreatedAtDesc(User user);
    
    // Find threads by date range
    @Query("SELECT t FROM Thread t WHERE t.createdAt BETWEEN :startDate AND :endDate AND t.isDeleted = false AND t.isPublic = true ORDER BY t.createdAt DESC")
    Page<Thread> findByDateRange(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate, Pageable pageable);
    
    // Find trending threads (most liked in last 24 hours)
    @Query("SELECT t FROM Thread t WHERE t.createdAt >= :since AND t.isDeleted = false AND t.isPublic = true ORDER BY t.likesCount DESC, t.createdAt DESC")
    Page<Thread> findTrendingThreads(@Param("since") java.time.LocalDateTime since, Pageable pageable);
    
    // Find threads by multiple users (for feed)
    @Query("SELECT t FROM Thread t WHERE t.user IN :users AND t.isDeleted = false AND t.isPublic = true ORDER BY t.createdAt DESC")
    Page<Thread> findByUsersIn(@Param("users") List<User> users, Pageable pageable);
    
    // Find threads with specific engagement threshold
    @Query("SELECT t FROM Thread t WHERE t.likesCount >= :minLikes AND t.isDeleted = false AND t.isPublic = true ORDER BY t.likesCount DESC, t.createdAt DESC")
    Page<Thread> findByMinLikes(@Param("minLikes") Integer minLikes, Pageable pageable);
    
    // Find main threads for a specific user (handles all 4 privacy levels)
    @Query("SELECT t FROM Thread t WHERE t.parentThread IS NULL AND t.isDeleted = false AND " +
           "(t.isPublic = true OR " + // Public threads (ANYONE)
           "(t.isPublic = false AND t.replyPermission = 'FOLLOWERS' AND t.user IN " +
           "(SELECT f.following FROM Follow f WHERE f.follower.id = :userId)) OR " + // Followers-only threads from followed users
           "(t.isPublic = false AND t.replyPermission = 'FOLLOWING' AND t.user.id = :userId) OR " + // My threads with FOLLOWING permission
           "(t.isPublic = false AND t.replyPermission = 'MENTIONED_ONLY' AND t.id IN " +
           "(SELECT tm.thread.id FROM ThreadMention tm WHERE tm.mentionedUser.id = :userId))) " + // Threads where I'm mentioned
           "ORDER BY t.createdAt DESC")
    Page<Thread> findMainThreadsForUser(@Param("userId") String userId, Pageable pageable);
}
