package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.ReviewRequest;
import com.ecommerce.backend.dto.ReviewResponse;
import com.ecommerce.backend.service.ReviewService;
import com.ecommerce.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/api/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(Principal principal, @PathVariable Long productId, @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review added successfully", reviewService.createReview(principal.getName(), productId, request)));
    }

    @GetMapping("/api/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved successfully", reviewService.getReviewsByProduct(productId)));
    }

    @PutMapping("/api/reviews/{id}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(Principal principal, @PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Review updated successfully", reviewService.updateReview(principal.getName(), id, request)));
    }

    @DeleteMapping("/api/reviews/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(Principal principal, @PathVariable Long id) {
        reviewService.deleteReview(principal.getName(), id);
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully", null));
    }
}
