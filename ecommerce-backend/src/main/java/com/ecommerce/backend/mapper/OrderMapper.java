package com.ecommerce.backend.mapper;

import com.ecommerce.backend.dto.OrderItemResponse;
import com.ecommerce.backend.dto.OrderResponse;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class OrderMapper {

    public static OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            OrderItemResponse response = new OrderItemResponse();
            response.setId(item.getId());
            response.setProductId(item.getProduct().getId());
            response.setProductName(item.getProduct().getName());
            response.setQuantity(item.getQuantity());
            response.setPrice(item.getPrice());
            response.setSubtotal(item.getSubtotal());
            items.add(response);
        }

        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUser().getId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus().name());
        response.setShippingAddress(order.getShippingAddress());
        response.setCreatedAt(order.getCreatedAt());
        response.setItems(items);
        return response;
    }
}
