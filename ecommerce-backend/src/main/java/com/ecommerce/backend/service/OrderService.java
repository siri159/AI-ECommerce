package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CreateOrderRequest;
import com.ecommerce.backend.dto.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(String email, CreateOrderRequest request);
    List<OrderResponse> getOrdersForCurrentUser(String email);
    OrderResponse getOrderById(String email, Long orderId);
    List<OrderResponse> getAllOrders();
    OrderResponse cancelOrder(String email, Long orderId);
    OrderResponse updateStatus(Long orderId, String status);
}
