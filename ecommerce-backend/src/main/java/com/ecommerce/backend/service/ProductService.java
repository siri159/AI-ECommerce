package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.ProductRequest;
import com.ecommerce.backend.dto.ProductResponse;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

public interface ProductService {
    Page<ProductResponse> getProducts(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, String sort, int page, int size);
    ProductResponse getProductById(Long id);
    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
    Page<ProductResponse> getProductsByCategory(Long categoryId, int page, int size);
}
