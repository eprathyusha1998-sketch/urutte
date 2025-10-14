package com.urutte.repository;

import com.urutte.model.AiGeneratedThread;
import com.urutte.model.Topic;
import com.urutte.model.AiAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AiGeneratedThreadRepository extends JpaRepository<AiGeneratedThread, Long> {
    
    List<AiGeneratedThread> findByTopicAndStatus(Topic topic, String status);
    
    List<AiGeneratedThread> findByAiAdminAndStatus(AiAdmin aiAdmin, String status);
    
    @Query("SELECT agt FROM AiGeneratedThread agt WHERE agt.status = 'active' ORDER BY agt.createdAt DESC")
    List<AiGeneratedThread> findActiveThreadsOrderByCreatedAtDesc();
    
    @Query("SELECT agt FROM AiGeneratedThread agt WHERE agt.topic = :topic AND agt.status = 'active' ORDER BY agt.createdAt DESC")
    List<AiGeneratedThread> findActiveThreadsByTopic(@Param("topic") Topic topic);
    
    @Query("SELECT COUNT(agt) FROM AiGeneratedThread agt WHERE agt.topic = :topic AND agt.status = 'active'")
    long countActiveThreadsByTopic(@Param("topic") Topic topic);
    
    @Query("SELECT COUNT(agt) FROM AiGeneratedThread agt WHERE agt.aiAdmin = :aiAdmin AND agt.status = 'active'")
    long countActiveThreadsByAiAdmin(@Param("aiAdmin") AiAdmin aiAdmin);
    
    @Query("SELECT agt FROM AiGeneratedThread agt WHERE agt.createdAt >= :since AND agt.status = 'active'")
    List<AiGeneratedThread> findThreadsCreatedSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT agt FROM AiGeneratedThread agt WHERE agt.generationMethod = :method AND agt.status = 'active'")
    List<AiGeneratedThread> findByGenerationMethod(@Param("method") String method);
}
