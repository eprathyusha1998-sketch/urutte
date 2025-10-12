package com.urutte.repository;

import com.urutte.model.Post;
import com.urutte.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    // Find posts by user
    Page<Post> findByUserOrderByTimestampDesc(User user, Pageable pageable);
    
    // Find posts by user ID
    List<Post> findByUserIdOrderByTimestampDesc(String userId);
    
    // Find replies to a post
    List<Post> findByParentPostOrderByTimestampAsc(Post parentPost);
    
    // Find main posts (not replies)
    Page<Post> findByParentPostIsNullOrderByTimestampDesc(Pageable pageable);
    
    // Search posts by content
    @Query("SELECT p FROM Post p WHERE p.content LIKE %:keyword% ORDER BY p.timestamp DESC")
    Page<Post> findByContentContaining(@Param("keyword") String keyword, Pageable pageable);
    
    // Find posts from followed users
    @Query("SELECT p FROM Post p WHERE p.user IN " +
           "(SELECT f.following FROM Follow f WHERE f.follower.id = :userId) " +
           "AND p.parentPost IS NULL ORDER BY p.timestamp DESC")
    Page<Post> findFeedPosts(@Param("userId") String userId, Pageable pageable);
    
    // Count posts by user
    long countByUser(User user);
    
    // Count replies to a post
    long countByParentPost(Post parentPost);
    
    // Find all posts in a thread hierarchy
    List<Post> findByRootPostOrderByThreadPathAsc(Post rootPost);
    
    // Find posts by root post (for thread traversal)
    List<Post> findByRootPost(Post rootPost);
}
