package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.CreateOrderRequest;
import com.ecommerce.backend.dto.OrderResponse;
import com.ecommerce.backend.service.OrderService;
import com.ecommerce.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/api/orders")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(Principal principal, @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", orderService.createOrder(principal.getName(), request)));
    }

    @GetMapping("/api/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getUserOrders(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orderService.getOrdersForCurrentUser(principal.getName())));
    }

    @GetMapping("/api/orders/my-orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orderService.getOrdersForCurrentUser(principal.getName())));
    }

    @GetMapping("/api/orders/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(Principal principal, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", orderService.getOrderById(principal.getName(), id)));
    }

    @PutMapping("/api/orders/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(Principal principal, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", orderService.cancelOrder(principal.getName(), id)));
    }

    @GetMapping("/api/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrdersForAdmin() {
        return ResponseEntity.ok(ApiResponse.success("Admin orders retrieved successfully", orderService.getAllOrders()));
    }

    @PutMapping("/api/admin/orders/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", orderService.updateStatus(id, status)));
    }
}
