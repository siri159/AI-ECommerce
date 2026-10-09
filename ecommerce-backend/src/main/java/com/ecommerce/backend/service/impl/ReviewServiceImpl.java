package com.ecommerce.backend.service.impl;

import com.ecommerce.backend.dto.ReviewRequest;
import com.ecommerce.backend.dto.ReviewResponse;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.Review;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.ReviewRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.service.AIService;
import com.ecommerce.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AIService aiService;

    @Override
    @Transactional
    public ReviewResponse createReview(String email, Long productId, ReviewRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (reviewRepository.findByUserAndProduct(user, product).isPresent()) {
            throw new IllegalArgumentException("You have already reviewed this product");
        }

        Review.Sentiment sentiment = aiService.analyzeSentiment(request.getComment());
        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .comment(request.getComment())
                .sentiment(sentiment)
                .build();

        Review savedReview = reviewRepository.save(review);
        return toResponse(savedReview);
    }

    @Override
    public List<ReviewResponse> getReviewsByProduct(Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return reviewRepository.findByProductId(productId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(String email, Long reviewId, ReviewRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only update your own review");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setSentiment(aiService.analyzeSentiment(request.getComment()));
        return toResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public void deleteReview(String email, Long reviewId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        if (!review.getUser().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("You are not allowed to delete this review");
        }

        reviewRepository.delete(review);
    }

    private ReviewResponse toResponse(Review review) {
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
