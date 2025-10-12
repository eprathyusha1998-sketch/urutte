package com.urutte.repository;

import com.urutte.model.Product;
import com.urutte.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Find products by category
    Page<Product> findByCategoryAndIsActiveTrue(String category, Pageable pageable);
    
    // Find products by seller
    Page<Product> findBySellerAndIsActiveTrue(User seller, Pageable pageable);
    
    // Search products by title or description
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);
    
    // Find products by price range
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice, Pageable pageable);
    
    // Find products by location
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND LOWER(p.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    Page<Product> findByLocation(@Param("location") String location, Pageable pageable);
    
    // Find active products
    Page<Product> findByIsActiveTrue(Pageable pageable);
    
    // Find products by multiple categories
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.category IN :categories")
    Page<Product> findByCategories(@Param("categories") List<String> categories, Pageable pageable);
    
    // Find trending products (most favorited)
    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY SIZE(p.favorites) DESC")
    Page<Product> findTrendingProducts(Pageable pageable);
    
    // Find recently added products
    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.createdAt DESC")
    Page<Product> findRecentProducts(Pageable pageable);
}
