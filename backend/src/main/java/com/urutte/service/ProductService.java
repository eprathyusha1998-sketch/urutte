package com.urutte.service;

import com.urutte.dto.CreateProductDto;
import com.urutte.dto.ProductDto;
import com.urutte.model.Product;
import com.urutte.model.ProductFavorite;
import com.urutte.model.ProductReview;
import com.urutte.model.User;
import com.urutte.repository.ProductFavoriteRepository;
import com.urutte.repository.ProductRepository;
import com.urutte.repository.ProductReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductReviewRepository productReviewRepository;
    
    @Autowired
    private ProductFavoriteRepository productFavoriteRepository;

    public ProductDto createProduct(CreateProductDto createProductDto, String userId) {
        User user = userService.getUserById(userId);
        
        Product product = new Product();
        product.setTitle(createProductDto.getTitle());
        product.setDescription(createProductDto.getDescription());
        product.setPrice(createProductDto.getPrice());
        product.setOriginalPrice(createProductDto.getOriginalPrice());
        product.setCategory(createProductDto.getCategory());
        product.setBrand(createProductDto.getBrand());
        product.setModel(createProductDto.getModel());
        product.setCondition(createProductDto.getCondition());
        product.setLocation(createProductDto.getLocation());
        product.setSeller(user);
        
        // Convert image URLs list to JSON string
        if (createProductDto.getImageUrls() != null && !createProductDto.getImageUrls().isEmpty()) {
            String imageUrlsJson = String.join(",", createProductDto.getImageUrls());
            product.setImageUrls(imageUrlsJson);
        }
        
        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct, userId);
    }

    public Page<ProductDto> getAllProducts(String currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByIsActiveTrue(pageable)
            .map(product -> convertToDto(product, currentUserId));
    }
    
    public Page<ProductDto> getProductsByCategory(String category, String currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByCategoryAndIsActiveTrue(category, pageable)
            .map(product -> convertToDto(product, currentUserId));
    }
    
    public Page<ProductDto> searchProducts(String keyword, String currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.searchProducts(keyword, pageable)
            .map(product -> convertToDto(product, currentUserId));
    }
    
    public Page<ProductDto> getProductsByPriceRange(Double minPrice, Double maxPrice, String currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByPriceRange(minPrice, maxPrice, pageable)
            .map(product -> convertToDto(product, currentUserId));
    }
    
    public Page<ProductDto> getTrendingProducts(String currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findTrendingProducts(pageable)
            .map(product -> convertToDto(product, currentUserId));
    }
    
    public ProductDto getProductById(Long productId, String currentUserId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        return convertToDto(product, currentUserId);
    }
    
    public ProductDto toggleFavorite(Long productId, String userId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        User user = userService.getUserById(userId);
        
        // Check if already favorited
        if (productFavoriteRepository.existsByProductAndUser(product, user)) {
            productFavoriteRepository.deleteByProductAndUser(product, user);
        } else {
            ProductFavorite favorite = new ProductFavorite(product, user);
            productFavoriteRepository.save(favorite);
        }
        
        return convertToDto(product, userId);
    }
    
    public ProductDto addReview(Long productId, Integer rating, String comment, String userId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        User user = userService.getUserById(userId);
        
        // Check if user already reviewed this product
        if (productReviewRepository.findByProductAndUser(product, user).isPresent()) {
            throw new RuntimeException("You have already reviewed this product");
        }
        
        ProductReview review = new ProductReview(product, user, rating, comment);
        productReviewRepository.save(review);
        
        return convertToDto(product, userId);
    }

    private ProductDto convertToDto(Product product, String currentUserId) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setOriginalPrice(product.getOriginalPrice());
        dto.setCategory(product.getCategory());
        dto.setBrand(product.getBrand());
        dto.setModel(product.getModel());
        dto.setCondition(product.getCondition());
        dto.setLocation(product.getLocation());
        dto.setIsActive(product.getIsActive());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        
        // Set seller information
        dto.setSellerId(product.getSeller().getId());
        dto.setSellerName(product.getSeller().getName());
        dto.setSellerAvatar(product.getSeller().getPicture());
        
        // Parse image URLs
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            List<String> imageUrls = List.of(product.getImageUrls().split(","));
            dto.setImageUrls(imageUrls);
        }
        
        // Set review statistics
        dto.setReviewCount(productReviewRepository.countByProduct(product));
        Double averageRating = productReviewRepository.getAverageRatingByProduct(product);
        dto.setAverageRating(averageRating != null ? averageRating : 0.0);
        
        // Set favorite statistics
        dto.setFavoriteCount(productFavoriteRepository.countByProduct(product));
        
        // Set user interaction status
        if (currentUserId != null) {
            User currentUser = userService.getUserById(currentUserId);
            dto.setIsFavorited(productFavoriteRepository.existsByProductAndUser(product, currentUser));
        }
        
        return dto;
    }
}
