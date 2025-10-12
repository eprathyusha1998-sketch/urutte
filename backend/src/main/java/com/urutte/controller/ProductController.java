package com.urutte.controller;

import com.urutte.dto.CreateProductDto;
import com.urutte.dto.ProductDto;
import com.urutte.model.User;
import com.urutte.service.ProductService;
import com.urutte.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody CreateProductDto createProductDto,
                                                  @AuthenticationPrincipal OidcUser principal) {
        User user = userService.getOrCreateUser(principal);
        ProductDto product = productService.createProduct(createProductDto, user.getId());
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<Page<ProductDto>> getAllProducts(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size,
                                                         @AuthenticationPrincipal OidcUser principal) {
        User user = userService.getOrCreateUser(principal);
        Page<ProductDto> products = productService.getAllProducts(user.getId(), page, size);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long productId,
                                                   @AuthenticationPrincipal OidcUser principal) {
        User user = userService.getOrCreateUser(principal);
        ProductDto product = productService.getProductById(productId, user.getId());
        return ResponseEntity.ok(product);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<ProductDto>> getProductsByCategory(@PathVariable String category,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "20") int size,
                                                                @AuthenticationPrincipal OidcUser principal) {
        User user = userService.getOrCreateUser(principal);
        Page<ProductDto> products = productService.getProductsByCategory(category, user.getId(), page, size);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto>> searchProducts(@RequestParam String q,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size,
                                                         @AuthenticationPrincipal OidcUser principal) {
        User user = userService.getOrCreateUser(principal);
        Page<ProductDto> products = productService.searchProducts(q, user.getId(), page, size);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/price-range")
    public ResponseEntity<Page<ProductDto>> getProductsByPriceRange(@RequestParam Double minPrice,
                                                                  @RequestParam Double maxPrice,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "20") int size,
                                                                  @AuthenticationPrincipal OidcUser principal) {
        User user = userService.getOrCreateUser(principal);
        Page<ProductDto> products = productService.getProductsByPriceRange(minPrice, maxPrice, user.getId(), page, size);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/trending")
    public ResponseEntity<Page<ProductDto>> getTrendingProducts(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size,
                                                              @AuthenticationPrincipal OidcUser principal) {
        User user = userService.getOrCreateUser(principal);
        Page<ProductDto> products = productService.getTrendingProducts(user.getId(), page, size);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/{productId}/favorite")
    public ResponseEntity<ProductDto> toggleFavorite(@PathVariable Long productId,
                                                   @AuthenticationPrincipal OidcUser principal) {
        User user = userService.getOrCreateUser(principal);
        ProductDto product = productService.toggleFavorite(productId, user.getId());
        return ResponseEntity.ok(product);
    }

    @PostMapping("/{productId}/review")
    public ResponseEntity<Map<String, Object>> addReview(@PathVariable Long productId,
                                                       @RequestParam Integer rating,
                                                       @RequestParam String comment,
                                                       @AuthenticationPrincipal OidcUser principal) {
        User user = userService.getOrCreateUser(principal);
        Map<String, Object> response = new HashMap<>();
        
        try {
            ProductDto product = productService.addReview(productId, rating, comment, user.getId());
            response.put("success", true);
            response.put("product", product);
            response.put("message", "Review added successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
