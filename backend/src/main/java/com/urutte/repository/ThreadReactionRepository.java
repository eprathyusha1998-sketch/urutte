package com.urutte.repository;

import com.urutte.model.Thread;
import com.urutte.model.ThreadReaction;
import com.urutte.model.User;
import com.urutte.model.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThreadReactionRepository extends JpaRepository<ThreadReaction, Long> {
    
    // Check if user reacted to a thread
    Optional<ThreadReaction> findByThreadAndUser(Thread thread, User user);
    
    // Check if user reacted to a thread by IDs
    boolean existsByThreadIdAndUserId(Long threadId, String userId);
    
    // Delete reaction by user and thread
    void deleteByThreadAndUser(Thread thread, User user);
    
    // Delete reaction by user and thread IDs
    void deleteByThreadIdAndUserId(Long threadId, String userId);
    
    // Find all reactions for a thread
    List<ThreadReaction> findByThreadOrderByCreatedAtDesc(Thread thread);
    
    // Find all reactions by a user
    List<ThreadReaction> findByUserOrderByCreatedAtDesc(User user);
    
    // Find reactions by type
    List<ThreadReaction> findByReactionTypeOrderByCreatedAtDesc(ReactionType reactionType);
}
