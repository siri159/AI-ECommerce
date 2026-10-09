package com.ecommerce.backend.service.impl;

import com.ecommerce.backend.dto.ProductResponse;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.Review;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.ReviewRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalUsers", userRepository.count());
        dashboard.put("totalProducts", productRepository.count());
        dashboard.put("totalOrders", orderRepository.count());
        dashboard.put("totalRevenue", orderRepository.findAll().stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        dashboard.put("pendingOrders", orderRepository.findByStatus(Order.OrderStatus.PENDING, org.springframework.data.domain.Sort.unsorted()).size());
        dashboard.put("lowStockProducts", productRepository.findAll().stream().filter(product -> product.getStockQuantity() <= 10).count());
        return dashboard;
    }

    @Override
    public Map<String, Object> getSalesSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRevenue", orderRepository.findAll().stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.put("ordersPlaced", orderRepository.count());
        summary.put("averageOrderValue", orderRepository.findAll().stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.max(orderRepository.count(), 1L)), java.math.MathContext.DECIMAL128));
        return summary;
    }

    @Override
    public List<ProductResponse> getLowStockProducts(int threshold) {
        return productRepository.findAll().stream()
                .filter(product -> product.getStockQuantity() <= threshold)
                .map(product -> {
                    ProductResponse response = new ProductResponse();
                    response.setId(product.getId());
                    response.setName(product.getName());
                    response.setPrice(product.getPrice());
                    response.setStockQuantity(product.getStockQuantity());
                    response.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : null);
                    return response;
                })
                .toList();
    }

    @Override
    public Map<String, Object> getAiInsights() {
        Map<String, Object> insights = new HashMap<>();
        insights.put("bestSellingProducts", productRepository.findAll().stream().limit(5).toList());
        insights.put("lowStockProducts", getLowStockProducts(10));
        insights.put("mostReviewedProducts", reviewRepository.findAll().stream()
                .collect(java.util.stream.Collectors.groupingBy(review -> review.getProduct().getId(), java.util.stream.Collectors.counting())));
        insights.put("sentimentBreakdown", Map.of(
                "POSITIVE", reviewRepository.findAll().stream().filter(review -> review.getSentiment() == Review.Sentiment.POSITIVE).count(),
                "NEUTRAL", reviewRepository.findAll().stream().filter(review -> review.getSentiment() == Review.Sentiment.NEUTRAL).count(),
                "NEGATIVE", reviewRepository.findAll().stream().filter(review -> review.getSentiment() == Review.Sentiment.NEGATIVE).count()
        ));
        insights.put("promotionCandidates", productRepository.findAll().stream()
                .filter(product -> product.getStockQuantity() <= 15)
                .map(Product::getName)
                .toList());
        return insights;
    }
}
