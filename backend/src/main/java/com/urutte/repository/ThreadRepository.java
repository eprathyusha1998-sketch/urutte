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
    
    // Find main threads (not replies) - excludes AI content for anonymous users
    @Query("SELECT t FROM Thread t WHERE t.parentThread IS NULL AND t.isDeleted = false AND t.isPublic = true AND t.user.userType != 'ADMIN' ORDER BY t.createdAt DESC")
    Page<Thread> findByParentThreadIsNullAndIsDeletedFalseAndIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);
    
    // Find replies to a specific thread
    List<Thread> findByParentThreadAndIsDeletedFalseOrderByCreatedAtAsc(Thread parentThread);
    
    // Find replies to a specific thread by parent thread ID
    @Query("SELECT t FROM Thread t WHERE t.parentThread.id = :parentThreadId AND t.isDeleted = false ORDER BY t.createdAt ASC")
    List<Thread> findByParentThreadIdAndIsDeletedFalseOrderByCreatedAtAsc(@Param("parentThreadId") Long parentThreadId);
    
    // Find all replies in a thread hierarchy
    List<Thread> findByRootThreadAndIsDeletedFalseOrderByThreadPathAsc(Thread rootThread);
    
    // Find threads by hashtag - excludes AI content unless user follows AI
    @Query("SELECT t FROM Thread t JOIN t.hashtags th JOIN th.hashtag h WHERE h.tag = :hashtag AND t.isDeleted = false AND t.isPublic = true AND t.user.userType != 'ADMIN' ORDER BY t.createdAt DESC")
    Page<Thread> findByHashtag(@Param("hashtag") String hashtag, Pageable pageable);
    
    // Find threads mentioning a user
    @Query("SELECT t FROM Thread t JOIN t.mentions tm WHERE tm.mentionedUser = :user AND t.isDeleted = false AND t.isPublic = true ORDER BY t.createdAt DESC")
    Page<Thread> findMentionsByUser(@Param("user") User user, Pageable pageable);
    
    // Find threads by content search - excludes AI content
    @Query("SELECT t FROM Thread t WHERE t.content LIKE %:keyword% AND t.isDeleted = false AND t.isPublic = true AND t.user.userType != 'ADMIN' ORDER BY t.createdAt DESC")
    Page<Thread> findByContentContaining(@Param("keyword") String keyword, Pageable pageable);
    
    // Find threads by user and content
    @Query("SELECT t FROM Thread t WHERE t.user = :user AND t.content LIKE %:keyword% AND t.isDeleted = false ORDER BY t.createdAt DESC")
    Page<Thread> findByUserAndContentContaining(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);
    
    // Find threads by thread type - excludes AI content
    @Query("SELECT t FROM Thread t WHERE t.threadType = :threadType AND t.isDeleted = false AND t.isPublic = true AND t.user.userType != 'ADMIN' ORDER BY t.createdAt DESC")
    Page<Thread> findByThreadTypeAndIsDeletedFalseAndIsPublicTrueOrderByCreatedAtDesc(@Param("threadType") com.urutte.model.ThreadType threadType, Pageable pageable);
    
    // Find quoted threads - excludes AI content
    @Query("SELECT t FROM Thread t WHERE t.quotedThread = :quotedThread AND t.isDeleted = false AND t.isPublic = true AND t.user.userType != 'ADMIN' ORDER BY t.createdAt DESC")
    Page<Thread> findByQuotedThread(@Param("quotedThread") Thread quotedThread, Pageable pageable);
    
    // Find threads by thread level
    List<Thread> findByThreadLevelAndIsDeletedFalseOrderByCreatedAtAsc(Integer threadLevel);
    
    // Find threads by thread path
    List<Thread> findByThreadPathStartingWithAndIsDeletedFalseOrderByThreadPathAsc(String threadPath);
    
    // Count threads by user
    long countByUserAndIsDeletedFalse(User user);
    
    // Count replies to a thread
    long countByParentThreadAndIsDeletedFalse(Thread parentThread);
    
    // Find threads with media - excludes AI content
    @Query("SELECT t FROM Thread t WHERE t.media IS NOT EMPTY AND t.isDeleted = false AND t.isPublic = true AND t.user.userType != 'ADMIN' ORDER BY t.createdAt DESC")
    Page<Thread> findThreadsWithMedia(Pageable pageable);
    
    // Find pinned threads by user
    List<Thread> findByUserAndIsPinnedTrueAndIsDeletedFalseOrderByCreatedAtDesc(User user);
    
    // Find threads by date range - excludes AI content
    @Query("SELECT t FROM Thread t WHERE t.createdAt BETWEEN :startDate AND :endDate AND t.isDeleted = false AND t.isPublic = true AND t.user.userType != 'ADMIN' ORDER BY t.createdAt DESC")
    Page<Thread> findByDateRange(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate, Pageable pageable);
    
    // Find trending threads (most liked in last 24 hours) - excludes AI content
    @Query("SELECT t FROM Thread t WHERE t.createdAt >= :since AND t.isDeleted = false AND t.isPublic = true AND t.user.userType != 'ADMIN' ORDER BY t.likesCount DESC, t.createdAt DESC")
    Page<Thread> findTrendingThreads(@Param("since") java.time.LocalDateTime since, Pageable pageable);
    
    // Find threads by multiple users (for feed) - excludes AI content
    @Query("SELECT t FROM Thread t WHERE t.user IN :users AND t.isDeleted = false AND t.isPublic = true AND t.user.userType != 'ADMIN' ORDER BY t.createdAt DESC")
    Page<Thread> findByUsersIn(@Param("users") List<User> users, Pageable pageable);
    
    // Find threads with specific engagement threshold - excludes AI content
    @Query("SELECT t FROM Thread t WHERE t.likesCount >= :minLikes AND t.isDeleted = false AND t.isPublic = true AND t.user.userType != 'ADMIN' ORDER BY t.likesCount DESC, t.createdAt DESC")
    Page<Thread> findByMinLikes(@Param("minLikes") Integer minLikes, Pageable pageable);
    
    // Find main threads for a specific user (handles all 4 privacy levels)
    // AI-generated content is only shown if user follows the AI user
    @Query("SELECT t FROM Thread t WHERE t.parentThread IS NULL AND t.isDeleted = false AND " +
           "(" +
           // Show ALL logged in user created threads in the feed (regardless of thread type)
           "t.user.id = :userId OR " +
           // Regular threads (non-ADMIN users) - exclude user's own threads to avoid duplication
           "(t.user.userType != 'ADMIN' AND t.user.id != :userId AND " +
           "(t.isPublic = true OR " + // Show all public posts from regular users
           "(t.isPublic = false AND t.replyPermission = 'FOLLOWERS' AND t.user IN " +
           "(SELECT f.following FROM Follow f WHERE f.follower.id = :userId)) OR " + // Show all followers-only posts from followed users
           "(t.isPublic = false AND t.replyPermission = 'FOLLOWING' AND t.user.id = :userId) OR " + // Show all my threads with FOLLOWING permission
           "(t.isPublic = false AND t.replyPermission = 'MENTIONED_ONLY' AND t.id IN " +
           "(SELECT tm.thread.id FROM ThreadMention tm WHERE tm.mentionedUser.id = :userId)))) " + // Show all threads where I'm mentioned
           "OR " +
           // AI-generated threads (only if user follows AI user)
           "(t.user.userType = 'ADMIN' AND EXISTS " +
           "(SELECT 1 FROM Follow f WHERE f.follower.id = :userId AND f.following.id = t.user.id))" +
           ") " +
           "ORDER BY CASE WHEN t.user.id = :userId THEN 0 ELSE 1 END, t.createdAt DESC")
    Page<Thread> findMainThreadsForUser(@Param("userId") String userId, Pageable pageable);
    
    // Find main threads for a specific user with AI topic filtering
    // AI-generated content is only shown if user follows the AI user AND the AI post is about topics the user likes
    @Query("SELECT t FROM Thread t WHERE t.parentThread IS NULL AND t.isDeleted = false AND " +
           "(" +
           // Show ALL logged in user created threads in the feed (regardless of thread type)
           "t.user.id = :userId OR " +
           // Regular threads (non-ADMIN users) - exclude user's own threads to avoid duplication
           "(t.user.userType != 'ADMIN' AND t.user.id != :userId AND " +
           "(t.isPublic = true OR " + // Show all public posts from regular users
           "(t.isPublic = false AND t.replyPermission = 'FOLLOWERS' AND t.user IN " +
           "(SELECT f.following FROM Follow f WHERE f.follower.id = :userId)) OR " + // Show all followers-only posts from followed users
           "(t.isPublic = false AND t.replyPermission = 'FOLLOWING' AND t.user.id = :userId) OR " + // Show all my threads with FOLLOWING permission
           "(t.isPublic = false AND t.replyPermission = 'MENTIONED_ONLY' AND t.id IN " +
           "(SELECT tm.thread.id FROM ThreadMention tm WHERE tm.mentionedUser.id = :userId)))) " + // Show all threads where I'm mentioned
           "OR " +
           // AI-generated threads (only if user follows AI user AND topic matches user's liked topics)
           "(t.user.userType = 'ADMIN' AND EXISTS " +
           "(SELECT 1 FROM Follow f WHERE f.follower.id = :userId AND f.following.id = t.user.id) AND " +
           "EXISTS (SELECT 1 FROM AiGeneratedThread agt JOIN agt.topic topic JOIN UserTopic ut " +
           "ON ut.topic.id = topic.id WHERE agt.thread.id = t.id AND ut.user.id = :userId))" +
           ") " +
           "ORDER BY CASE WHEN t.user.id = :userId THEN 0 ELSE 1 END, t.createdAt DESC")
    Page<Thread> findMainThreadsForUserWithTopicFilter(@Param("userId") String userId, Pageable pageable);
}
