package com.urutte.repository;

import com.urutte.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TopicRepository extends JpaRepository<Topic, String> {
    
    List<Topic> findByIsActiveTrueOrderByPriorityDesc();
    
    @Query("SELECT t FROM Topic t WHERE t.isActive = true AND t.lastGeneratedAt < :cutoffTime ORDER BY t.priority DESC, t.lastGeneratedAt ASC")
    List<Topic> findTopicsReadyForGeneration(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT t FROM Topic t WHERE t.isActive = true AND t.category = :category ORDER BY t.priority DESC")
    List<Topic> findByCategoryAndIsActiveTrue(@Param("category") String category);
    
    List<Topic> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
    
    @Query("SELECT DISTINCT t.category FROM Topic t WHERE t.isActive = true ORDER BY t.category")
    List<String> findDistinctCategories();
    
    @Query("SELECT COUNT(t) FROM Topic t WHERE t.isActive = true")
    long countActiveTopics();
}
