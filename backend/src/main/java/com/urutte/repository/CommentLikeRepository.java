package com.urutte.repository;

import com.urutte.model.Comment;
import com.urutte.model.CommentLike;
import com.urutte.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    
    // Check if user liked a comment
    Optional<CommentLike> findByUserAndComment(User user, Comment comment);
    
    // Count likes for a comment
    long countByComment(Comment comment);
    
    // Delete like by user and comment
    void deleteByUserAndComment(User user, Comment comment);
    
    // Delete like by user and comment IDs
    void deleteByUserIdAndCommentId(String userId, Long commentId);
    
    // Delete all likes for a comment
    void deleteByComment(Comment comment);
    
    // Check if user liked a comment by IDs
    boolean existsByUserIdAndCommentId(String userId, Long commentId);
}
