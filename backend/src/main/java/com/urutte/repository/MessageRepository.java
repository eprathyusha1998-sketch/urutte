package com.urutte.repository;

import com.urutte.model.Message;
import com.urutte.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1) ORDER BY m.createdAt ASC")
    List<Message> findConversationBetweenUsers(User user1, User user2);
    
    @Query("SELECT m FROM Message m WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1) ORDER BY m.createdAt DESC")
    Page<Message> findConversationBetweenUsersPaged(User user1, User user2, Pageable pageable);
    
    @Query("SELECT DISTINCT m.receiver FROM Message m WHERE m.sender = :user")
    List<User> findUsersMessagedBy(User user);
    
    @Query("SELECT DISTINCT m.sender FROM Message m WHERE m.receiver = :user")
    List<User> findUsersWhoMessaged(User user);
    
    @Query("SELECT m FROM Message m WHERE m.receiver = :user AND m.isRead = false ORDER BY m.createdAt DESC")
    List<Message> findUnreadMessages(User user);
    
    long countByReceiverAndIsReadFalse(User user);
    
}
