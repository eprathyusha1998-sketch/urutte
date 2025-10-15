package com.urutte.repository;

import com.urutte.model.Thread;
import com.urutte.model.ThreadHashtag;
import com.urutte.model.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThreadHashtagRepository extends JpaRepository<ThreadHashtag, Long> {
    
    // Find all hashtags for a thread
    List<ThreadHashtag> findByThreadOrderByCreatedAtDesc(Thread thread);
    
    // Find all threads with a hashtag
    List<ThreadHashtag> findByHashtagOrderByCreatedAtDesc(Hashtag hashtag);
    
    // Count hashtags for a thread
    long countByThread(Thread thread);
    
    // Count threads with a hashtag
    long countByHashtag(Hashtag hashtag);
    
    // Check if a thread-hashtag relationship already exists
    boolean existsByThreadAndHashtag(Thread thread, Hashtag hashtag);
}
