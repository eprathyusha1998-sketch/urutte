package com.urutte.repository;

import com.urutte.model.Like;
import com.urutte.model.Post;
import com.urutte.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    
    // Check if user liked a post
    Optional<Like> findByUserAndPost(User user, Post post);
    
    // Count likes for a post
    long countByPost(Post post);
    
    // Delete like by user and post
    void deleteByUserAndPost(User user, Post post);
    
    // Delete like by user and post IDs
    void deleteByUserIdAndPostId(String userId, Long postId);
    
    // Check if user liked a post by IDs
    boolean existsByUserIdAndPostId(String userId, Long postId);
    
    // Delete all likes for a post
    void deleteByPost(Post post);
}
