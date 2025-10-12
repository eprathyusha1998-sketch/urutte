package com.urutte.repository;

import com.urutte.model.Thread;
import com.urutte.model.ThreadView;
import com.urutte.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThreadViewRepository extends JpaRepository<ThreadView, Long> {
    
    // Find all views for a thread
    List<ThreadView> findByThreadOrderByCreatedAtDesc(Thread thread);
    
    // Find all views by a user
    List<ThreadView> findByUserOrderByCreatedAtDesc(User user);
    
    // Count views for a thread
    long countByThread(Thread thread);
    
    // Count views by user
    long countByUser(User user);
}
