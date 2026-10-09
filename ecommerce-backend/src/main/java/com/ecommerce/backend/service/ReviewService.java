package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.ReviewRequest;
import com.ecommerce.backend.dto.ReviewResponse;

import java.util.List;

public interface ReviewService {
    ReviewResponse createReview(String email, Long productId, ReviewRequest request);
    List<ReviewResponse> getReviewsByProduct(Long productId);
    ReviewResponse updateReview(String email, Long reviewId, ReviewRequest request);
    void deleteReview(String email, Long reviewId);
}
