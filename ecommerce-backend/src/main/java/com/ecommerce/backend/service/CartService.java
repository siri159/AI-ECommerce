package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.AddToCartRequest;
import com.ecommerce.backend.dto.CartResponse;
import com.ecommerce.backend.dto.UpdateCartRequest;

public interface CartService {
    CartResponse getCurrentUserCart(String email);
    CartResponse addItemToCart(String email, AddToCartRequest request);
    CartResponse updateCartItem(String email, Long itemId, UpdateCartRequest request);
    void removeCartItem(String email, Long itemId);
    void clearCart(String email);
}
