package com.ecommerce.backend.mapper;

import com.ecommerce.backend.dto.CartItemResponse;
import com.ecommerce.backend.dto.CartResponse;
import com.ecommerce.backend.entity.Cart;
import com.ecommerce.backend.entity.CartItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartMapper {

    public static CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subtotal);
            CartItemResponse response = new CartItemResponse();
            response.setId(item.getId());
            response.setProductId(item.getProduct().getId());
            response.setProductName(item.getProduct().getName());
            response.setQuantity(item.getQuantity());
            response.setPrice(item.getPrice());
            response.setSubtotal(subtotal);
            items.add(response);
        }

        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setUserId(cart.getUser().getId());
        response.setItems(items);
        response.setTotalAmount(total);
        return response;
    }
}
