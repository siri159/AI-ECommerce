package com.ecommerce.backend.mapper;

import com.ecommerce.backend.dto.ReviewResponse;
import com.ecommerce.backend.entity.Review;

public class ReviewMapper {

    public static ReviewResponse toResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setProductId(review.getProduct().getId());
        response.setUserId(review.getUser().getId());
        response.setUserName(review.getUser().getName());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setSentiment(review.getSentiment().name());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}
