package com.urutte.repository;

import com.urutte.model.Thread;
import com.urutte.model.ThreadMention;
import com.urutte.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThreadMentionRepository extends JpaRepository<ThreadMention, Long> {
    
    // Find all mentions for a thread
    List<ThreadMention> findByThreadOrderByCreatedAtDesc(Thread thread);
    
    // Find all mentions of a user
    List<ThreadMention> findByMentionedUserOrderByCreatedAtDesc(User mentionedUser);
    
    // Count mentions for a thread
    long countByThread(Thread thread);
    
    // Count mentions of a user
    long countByMentionedUser(User mentionedUser);
}
