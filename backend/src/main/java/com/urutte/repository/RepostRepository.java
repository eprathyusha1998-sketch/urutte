package com.urutte.repository;

import com.urutte.model.Post;
import com.urutte.model.Repost;
import com.urutte.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepostRepository extends JpaRepository<Repost, Long> {
    
    // Check if user reposted a post
    Optional<Repost> findByUserAndOriginalPost(User user, Post originalPost);
    
    // Count reposts for a post
    long countByOriginalPost(Post originalPost);
    
    // Delete repost by user and post
    void deleteByUserAndOriginalPost(User user, Post originalPost);
    
    // Delete repost by user and post IDs
    void deleteByUserIdAndOriginalPostId(String userId, Long postId);
    
    // Check if user reposted a post by IDs
    boolean existsByUserIdAndOriginalPostId(String userId, Long postId);
    
    // Delete all reposts for a post
    void deleteByOriginalPost(Post originalPost);
}
