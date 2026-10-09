package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.AIChatResponse;
import com.ecommerce.backend.dto.RecommendationResponse;
import com.ecommerce.backend.entity.Review;

import java.util.List;
import java.util.Map;

public interface AIService {
    AIChatResponse chat(String message, String email);
    List<RecommendationResponse> getRecommendations(String email);
    Review.Sentiment analyzeSentiment(String text);
    Map<String, Object> getAiInsights();
}
