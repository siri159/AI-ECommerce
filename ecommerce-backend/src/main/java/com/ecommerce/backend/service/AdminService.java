package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.ProductResponse;

import java.util.List;
import java.util.Map;

public interface AdminService {
    Map<String, Object> getDashboard();
    Map<String, Object> getSalesSummary();
    List<ProductResponse> getLowStockProducts(int threshold);
    Map<String, Object> getAiInsights();
}
