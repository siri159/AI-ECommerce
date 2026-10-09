package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.AddToCartRequest;
import com.ecommerce.backend.dto.CartResponse;
import com.ecommerce.backend.dto.UpdateCartRequest;
import com.ecommerce.backend.service.CartService;
import com.ecommerce.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully",
                cartService.getCurrentUserCart(principal.getName())));
    }

    @PostMapping("/cart/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItemToCart(Principal principal, @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Item added to cart successfully",
                cartService.addItemToCart(principal.getName(), request)));
    }

    @PutMapping("/cart/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(Principal principal, @PathVariable Long itemId, @Valid @RequestBody UpdateCartRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully",
                cartService.updateCartItem(principal.getName(), itemId, request)));
    }

    @DeleteMapping("/cart/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> removeCartItem(Principal principal, @PathVariable Long itemId) {
        cartService.removeCartItem(principal.getName(), itemId);
        return ResponseEntity.ok(ApiResponse.success("Cart item removed successfully", null));
    }

    @DeleteMapping("/cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(Principal principal) {
        cartService.clearCart(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", null));
    }
}
