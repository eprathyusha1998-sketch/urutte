package com.urutte.repository;

import com.urutte.model.Thread;
import com.urutte.model.ThreadRepost;
import com.urutte.model.User;
import com.urutte.model.RepostType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThreadRepostRepository extends JpaRepository<ThreadRepost, Long> {
    
    // Check if user reposted a thread
    Optional<ThreadRepost> findByThreadAndUser(Thread thread, User user);
    
    // Count reposts for a thread
    long countByThread(Thread thread);
    
    // Delete repost by user and thread
    void deleteByThreadAndUser(Thread thread, User user);
    
    // Delete repost by user and thread IDs
    void deleteByThreadIdAndUserId(Long threadId, String userId);
    
    // Check if user reposted a thread by IDs
    boolean existsByThreadIdAndUserId(Long threadId, String userId);
    
    // Find all reposts for a thread
    List<ThreadRepost> findByThreadOrderByCreatedAtDesc(Thread thread);
    
    // Find all reposts by a user
    List<ThreadRepost> findByUserOrderByCreatedAtDesc(User user);
    
    // Find reposts by type
    List<ThreadRepost> findByRepostTypeOrderByCreatedAtDesc(RepostType repostType);
    
    // Find quote reposts
    List<ThreadRepost> findByRepostTypeAndQuoteContentIsNotNullOrderByCreatedAtDesc(RepostType repostType);
    
    // Find recent reposts
    @Query("SELECT tr FROM ThreadRepost tr ORDER BY tr.createdAt DESC")
    List<ThreadRepost> findRecentReposts();
    
    // Find reposts by date range
    @Query("SELECT tr FROM ThreadRepost tr WHERE tr.createdAt BETWEEN :startDate AND :endDate ORDER BY tr.createdAt DESC")
    List<ThreadRepost> findByDateRange(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find most reposted threads
    @Query("SELECT tr.thread, COUNT(tr) as repostCount FROM ThreadRepost tr GROUP BY tr.thread ORDER BY repostCount DESC")
    List<Object[]> findMostRepostedThreads();
    
    // Find users who reposted a thread
    @Query("SELECT tr.user FROM ThreadRepost tr WHERE tr.thread = :thread ORDER BY tr.createdAt DESC")
    List<User> findUsersWhoRepostedThread(@Param("thread") Thread thread);
    
    // Find threads reposted by user
    @Query("SELECT tr.thread FROM ThreadRepost tr WHERE tr.user = :user ORDER BY tr.createdAt DESC")
    List<Thread> findThreadsRepostedByUser(@Param("user") User user);
    
    // Find quote reposts by user
    @Query("SELECT tr FROM ThreadRepost tr WHERE tr.user = :user AND tr.repostType = 'QUOTE' ORDER BY tr.createdAt DESC")
    List<ThreadRepost> findQuoteRepostsByUser(@Param("user") User user);
    
    // Find reposts with quote content
    @Query("SELECT tr FROM ThreadRepost tr WHERE tr.quoteContent IS NOT NULL AND tr.quoteContent != '' ORDER BY tr.createdAt DESC")
    List<ThreadRepost> findRepostsWithQuoteContent();
}
