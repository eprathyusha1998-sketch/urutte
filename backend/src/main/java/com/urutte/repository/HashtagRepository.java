package com.urutte.repository;

import com.urutte.model.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
    
    // Find hashtag by tag name
    Optional<Hashtag> findByTag(String tag);
    
    // Find hashtags by usage count
    List<Hashtag> findByUsageCountGreaterThanOrderByUsageCountDesc(Integer minUsageCount);
    
    // Find trending hashtags (most used)
    @Query("SELECT h FROM Hashtag h ORDER BY h.usageCount DESC, h.updatedAt DESC")
    List<Hashtag> findTrendingHashtags();
    
    // Find hashtags by partial tag name
    @Query("SELECT h FROM Hashtag h WHERE h.tag LIKE %:partialTag% ORDER BY h.usageCount DESC")
    List<Hashtag> findByTagContaining(@Param("partialTag") String partialTag);
    
    // Find hashtags with minimum usage count
    @Query("SELECT h FROM Hashtag h WHERE h.usageCount >= :minUsage ORDER BY h.usageCount DESC")
    List<Hashtag> findByMinUsageCount(@Param("minUsage") Integer minUsage);
    
    // Find most recently used hashtags
    @Query("SELECT h FROM Hashtag h ORDER BY h.updatedAt DESC")
    List<Hashtag> findRecentlyUsedHashtags();
    
    // Find hashtags by date range
    @Query("SELECT h FROM Hashtag h WHERE h.updatedAt BETWEEN :startDate AND :endDate ORDER BY h.usageCount DESC")
    List<Hashtag> findByDateRange(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find popular hashtags (used more than average)
    @Query("SELECT h FROM Hashtag h WHERE h.usageCount > (SELECT AVG(h2.usageCount) FROM Hashtag h2) ORDER BY h.usageCount DESC")
    List<Hashtag> findPopularHashtags();
    
    // Find hashtags with specific usage count
    List<Hashtag> findByUsageCountOrderByUpdatedAtDesc(Integer usageCount);
    
    // Find hashtags by tag pattern
    @Query("SELECT h FROM Hashtag h WHERE h.tag LIKE :pattern ORDER BY h.usageCount DESC")
    List<Hashtag> findByTagPattern(@Param("pattern") String pattern);
    
    // Count total hashtags
    long count();
    
    // Find hashtags with zero usage
    List<Hashtag> findByUsageCountOrderByCreatedAtDesc(Integer usageCount);
    
    // Find hashtags created in date range
    @Query("SELECT h FROM Hashtag h WHERE h.createdAt BETWEEN :startDate AND :endDate ORDER BY h.createdAt DESC")
    List<Hashtag> findCreatedInDateRange(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
}
