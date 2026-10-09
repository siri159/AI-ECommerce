package com.ecommerce.backend.service.impl;

import com.ecommerce.backend.dto.AIChatResponse;
import com.ecommerce.backend.dto.RecommendationResponse;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.Review;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.ReviewRepository;
import com.ecommerce.backend.service.AIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private static final Set<String> NON_SEARCH_TERMS = Set.of(
            "a", "an", "and", "below", "for", "from", "i", "looking", "me", "need",
            "please", "recommend", "suggest", "than", "the", "to", "under", "want", "with"
    );

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.api.key:}")
    private String apiKey;

    @Value("${ai.api.url:}")
    private String apiUrl;

    @Override
    public AIChatResponse chat(String message, String email) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message is required");
        }

        String keyword = extractKeyword(message);
        List<Product> products = productRepository.searchProducts(keyword, null, null, null, PageRequest.of(0, 5)).getContent();
        if (products.isEmpty()) {
            String[] terms = message.replaceAll("[^a-zA-Z0-9 ]", " ").toLowerCase().trim().split("\\s+");
            for (String term : terms) {
                if (term.length() < 3 || term.matches("\\d+") || NON_SEARCH_TERMS.contains(term)) {
                    continue;
                }
                products = productRepository.searchProducts(term, null, null, null, PageRequest.of(0, 5)).getContent();
                if (!products.isEmpty()) {
                    break;
                }
            }
        }
        String answer = buildFallbackAnswer(message, products);
        List<RecommendationResponse> recommendations = toRecommendationResponses(products, "Recommended based on your shopping request");

        if (apiKey != null && !apiKey.isBlank() && apiUrl != null && !apiUrl.isBlank()) {
            try {
                HttpClient client = HttpClient.newHttpClient();
                Map<String, Object> payload = new HashMap<>();
                payload.put("message", message);
                payload.put("context", products.stream().map(Product::getName).toList());
                String body = objectMapper.writeValueAsString(payload);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300 && !response.body().isBlank()) {
                    Map<String, Object> parsed = objectMapper.readValue(response.body(), Map.class);
                    if (parsed.containsKey("answer")) {
                        answer = String.valueOf(parsed.get("answer"));
                    }
                }
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return new AIChatResponse(answer, recommendations);
    }

    @Override
    public List<RecommendationResponse> getRecommendations(String email) {
        List<Product> products = productRepository.findTop10ByActiveTrueOrderByCreatedAtDesc();
        return toRecommendationResponses(products, "Popular choices for you");
    }

    @Override
    public Review.Sentiment analyzeSentiment(String text) {
        if (text == null || text.isBlank()) {
            return Review.Sentiment.NEUTRAL;
        }
        String lower = text.toLowerCase();
        if (lower.contains("love") || lower.contains("excellent") || lower.contains("great") || lower.contains("amazing") || lower.contains("good")) {
            return Review.Sentiment.POSITIVE;
        }
        if (lower.contains("bad") || lower.contains("terrible") || lower.contains("poor") || lower.contains("hate") || lower.contains("worst")) {
            return Review.Sentiment.NEGATIVE;
        }
        return Review.Sentiment.NEUTRAL;
    }

    @Override
    public Map<String, Object> getAiInsights() {
        Map<String, Object> insights = new HashMap<>();
        List<Product> lowStock = productRepository.findAll().stream()
                .filter(product -> product.getStockQuantity() <= 5)
                .limit(10)
                .toList();

        Map<String, Long> sentimentCounts = new HashMap<>();
        for (Review.Sentiment sentiment : Review.Sentiment.values()) {
            sentimentCounts.put(sentiment.name(), reviewRepository.findAll().stream()
                    .filter(review -> review.getSentiment() == sentiment)
                    .count());
        }

        insights.put("bestSellingProducts", productRepository.findTop10ByActiveTrueOrderByCreatedAtDesc());
        insights.put("lowStockProducts", lowStock);
        insights.put("mostReviewedProducts", reviewRepository.findAll().stream()
                .collect(java.util.stream.Collectors.groupingBy(review -> review.getProduct().getId(), java.util.stream.Collectors.counting())));
        insights.put("sentimentTrend", sentimentCounts);
        insights.put("recommendedPromotion", productRepository.findTop10ByActiveTrueOrderByCreatedAtDesc());
        return insights;
    }

    private String extractKeyword(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        String cleaned = message.replaceAll("[^a-zA-Z0-9 ]", " ").trim();
        return cleaned.length() > 30 ? cleaned.substring(0, 30) : cleaned;
    }

    private String buildFallbackAnswer(String message, List<Product> products) {
        if (products.isEmpty()) {
            return "I could not find a direct match in the catalog. Please try a broader search like laptop, smartphone, headphones, or camera.";
        }
        return "Based on your request for '" + message + "', here are the best matching products currently available in our catalog: "
                + products.stream().map(Product::getName).reduce((a, b) -> a + ", " + b).orElse("No items found");
    }

    private List<RecommendationResponse> toRecommendationResponses(List<Product> products, String reason) {
        List<RecommendationResponse> responses = new ArrayList<>();
        for (Product product : products) {
            RecommendationResponse recommendation = new RecommendationResponse();
            recommendation.setProductId(product.getId());
            recommendation.setName(product.getName());
            recommendation.setPrice(product.getPrice());
            recommendation.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : "General");
            recommendation.setReason(reason);
            responses.add(recommendation);
        }
        return responses;
    }
}
