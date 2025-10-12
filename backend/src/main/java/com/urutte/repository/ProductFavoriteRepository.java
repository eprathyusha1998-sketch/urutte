package com.urutte.repository;

import com.urutte.model.Product;
import com.urutte.model.ProductFavorite;
import com.urutte.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductFavoriteRepository extends JpaRepository<ProductFavorite, Long> {
    
    // Find favorites by user
    List<ProductFavorite> findByUserOrderByCreatedAtDesc(User user);
    
    // Find favorites by product
    List<ProductFavorite> findByProduct(Product product);
    
    // Find favorite by product and user
    Optional<ProductFavorite> findByProductAndUser(Product product, User user);
    
    // Check if user has favorited a product
    boolean existsByProductAndUser(Product product, User user);
    
    // Count favorites for a product
    long countByProduct(Product product);
    
    // Delete favorite by product and user
    void deleteByProductAndUser(Product product, User user);
}
