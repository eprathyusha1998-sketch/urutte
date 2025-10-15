package com.urutte.repository;

import com.urutte.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, String> {
    
    Optional<Topic> findByName(String name);
    
    List<Topic> findByIsActiveTrue();
    
    @Query(value = "SELECT * FROM topics t WHERE t.is_active = true AND t.id NOT IN " +
                   "(SELECT ut.topic_id FROM user_topics ut WHERE ut.user_id = :userId)", 
           nativeQuery = true)
    List<Topic> findAvailableTopicsForUser(@Param("userId") String userId);
    
    @Query("SELECT t FROM Topic t WHERE t.isActive = true ORDER BY RANDOM()")
    List<Topic> findRandomActiveTopics();
    
    List<Topic> findByIsActiveTrueOrderByPriorityDesc();
    
    @Query("SELECT t FROM Topic t WHERE t.isActive = true AND (t.lastGeneratedAt IS NULL OR t.lastGeneratedAt < :cutoffTime)")
    List<Topic> findTopicsReadyForGeneration(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    List<Topic> findByCategory(String category);
    
    @Query("SELECT DISTINCT t.category FROM Topic t WHERE t.category IS NOT NULL")
    List<String> findDistinctCategories();
    
    @Query("SELECT t FROM Topic t WHERE t.isActive = true AND " +
           "(LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.keywords) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Topic> searchTopics(@Param("query") String query);
}