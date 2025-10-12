package com.urutte.repository;

import com.urutte.model.Thread;
import com.urutte.model.ThreadBookmark;
import com.urutte.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThreadBookmarkRepository extends JpaRepository<ThreadBookmark, Long> {
    
    // Check if user bookmarked a thread
    Optional<ThreadBookmark> findByThreadAndUser(Thread thread, User user);
    
    // Check if user bookmarked a thread by IDs
    boolean existsByThreadIdAndUserId(Long threadId, String userId);
    
    // Delete bookmark by user and thread
    void deleteByThreadAndUser(Thread thread, User user);
    
    // Delete bookmark by user and thread IDs
    void deleteByThreadIdAndUserId(Long threadId, String userId);
    
    // Find all bookmarks for a thread
    List<ThreadBookmark> findByThreadOrderByCreatedAtDesc(Thread thread);
    
    // Find all bookmarks by a user
    List<ThreadBookmark> findByUserOrderByCreatedAtDesc(User user);
}
