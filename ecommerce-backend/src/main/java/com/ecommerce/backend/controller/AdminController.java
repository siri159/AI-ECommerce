package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.ProductResponse;
import com.ecommerce.backend.service.AdminService;
import com.ecommerce.backend.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard loaded successfully", adminService.getDashboard()));
    }

    @GetMapping("/sales")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSalesSummary() {
        return ResponseEntity.ok(ApiResponse.success("Sales summary loaded successfully", adminService.getSalesSummary()));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStockProducts(@RequestParam(defaultValue = "10") int threshold) {
        return ResponseEntity.ok(ApiResponse.success("Low stock products loaded successfully", adminService.getLowStockProducts(threshold)));
    }

    @GetMapping("/ai-insights")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAiInsights() {
        return ResponseEntity.ok(ApiResponse.success("AI insights loaded successfully", adminService.getAiInsights()));
    }
}
