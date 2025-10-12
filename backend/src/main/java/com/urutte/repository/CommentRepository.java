package com.urutte.repository;

import com.urutte.model.Comment;
import com.urutte.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // Get all comments for a post (top-level comments only)
    Page<Comment> findByPostAndParentCommentIsNullOrderByCreatedAtDesc(Post post, Pageable pageable);
    
    // Get replies for a specific comment
    List<Comment> findByParentCommentOrderByCreatedAtAsc(Comment parentComment);
    
    // Count comments for a post
    long countByPost(Post post);
    
    // Count main comments for a post (excluding replies)
    long countByPostAndParentCommentIsNull(Post post);
    
    // Count replies for a comment
    long countByParentComment(Comment parentComment);
    
    // Get all comments for a post (including replies)
    @Query("SELECT c FROM Comment c WHERE c.post = :post ORDER BY c.createdAt DESC")
    List<Comment> findAllByPostOrderByCreatedAtDesc(@Param("post") Post post);
    
    // Delete all comments for a post
    void deleteByPost(Post post);
}
