package com.urutte.repository;

import com.urutte.model.Product;
import com.urutte.model.ProductReview;
import com.urutte.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    
    // Find reviews by product
    List<ProductReview> findByProductOrderByCreatedAtDesc(Product product);
    
    // Find reviews by user
    List<ProductReview> findByUserOrderByCreatedAtDesc(User user);
    
    // Find review by product and user
    Optional<ProductReview> findByProductAndUser(Product product, User user);
    
    // Count reviews for a product
    long countByProduct(Product product);
    
    // Calculate average rating for a product
    @Query("SELECT AVG(pr.rating) FROM ProductReview pr WHERE pr.product = :product")
    Double getAverageRatingByProduct(@Param("product") Product product);
    
    // Find reviews by rating
    List<ProductReview> findByProductAndRating(Product product, Integer rating);
    
    // Delete review by product and user
    void deleteByProductAndUser(Product product, User user);
}
