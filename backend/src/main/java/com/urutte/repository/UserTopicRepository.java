package com.urutte.repository;

import com.urutte.model.Topic;
import com.urutte.model.UserTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTopicRepository extends JpaRepository<UserTopic, Long> {
    
    List<UserTopic> findByUserIdOrderByCreatedAtDesc(String userId);
    
    @Query("SELECT ut FROM UserTopic ut WHERE ut.user.id = :userId AND CAST(ut.topic.id AS string) = :topicId")
    Optional<UserTopic> findByUserIdAndTopicId(@Param("userId") String userId, @Param("topicId") String topicId);
    
    @Query("SELECT ut FROM UserTopic ut WHERE ut.user.id = :userId ORDER BY ut.createdAt ASC")
    List<UserTopic> findByUserIdOrderByCreatedAtAsc(@Param("userId") String userId);
    
    @Modifying
    @Query("DELETE FROM UserTopic ut WHERE ut.user.id = :userId AND CAST(ut.topic.id AS string) = :topicId")
    void deleteByUserIdAndTopicId(@Param("userId") String userId, @Param("topicId") String topicId);
    
    @Query("SELECT COUNT(ut) FROM UserTopic ut WHERE ut.user.id = :userId")
    long countByUserId(@Param("userId") String userId);
    
    @Query("SELECT t FROM Topic t JOIN UserTopic ut ON t.id = ut.topic.id WHERE ut.user.id = :userId ORDER BY ut.createdAt DESC")
    List<Topic> findLikedTopicsByUserId(@Param("userId") String userId);
}
