package com.ecommerce.backend.service.impl;

import com.ecommerce.backend.dto.AddToCartRequest;
import com.ecommerce.backend.dto.CartItemResponse;
import com.ecommerce.backend.dto.CartResponse;
import com.ecommerce.backend.dto.UpdateCartRequest;
import com.ecommerce.backend.entity.Cart;
import com.ecommerce.backend.entity.CartItem;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.CartItemRepository;
import com.ecommerce.backend.repository.CartRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Override
    public CartResponse getCurrentUserCart(String email) {
        User user = findUser(email);
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> createCart(user));
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItemToCart(String email, AddToCartRequest request) {
        User user = findUser(email);
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> createCart(user));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        if (!product.isActive()) {
            throw new IllegalArgumentException("This product is currently unavailable");
        }
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException("Not enough stock for product: " + product.getName());
        }

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (newQuantity > product.getStockQuantity()) {
                throw new IllegalArgumentException("Requested quantity exceeds available stock");
            }
            existingItem.setQuantity(newQuantity);
            existingItem.setPrice(product.getPrice());
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .price(product.getPrice())
                    .build();
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        return toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(String email, Long itemId, UpdateCartRequest request) {
        User user = findUser(email);
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + email));

        CartItem item = cart.getItems().stream()
                .filter(cartItem -> cartItem.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        if (item.getProduct().getStockQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException("Not enough stock for product: " + item.getProduct().getName());
        }

        item.setQuantity(request.getQuantity());
        item.setPrice(item.getProduct().getPrice());
        cartItemRepository.save(item);

        return toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public void removeCartItem(String email, Long itemId) {
        User user = findUser(email);
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + email));

        CartItem item = cart.getItems().stream()
                .filter(cartItem -> cartItem.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearCart(String email) {
        User user = findUser(email);
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + email));
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private Cart createCart(User user) {
        Cart cart = Cart.builder().user(user).build();
        return cartRepository.save(cart);
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> itemResponses = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subtotal);
            CartItemResponse response = new CartItemResponse();
            response.setId(item.getId());
            response.setProductId(item.getProduct().getId());
            response.setProductName(item.getProduct().getName());
            response.setProductImageUrl(item.getProduct().getImageUrl());
            response.setQuantity(item.getQuantity());
            response.setPrice(item.getPrice());
            response.setSubtotal(subtotal);
            itemResponses.add(response);
        }

        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setUserId(cart.getUser().getId());
        response.setItems(itemResponses);
        response.setTotalAmount(total);
        return response;
    }
}
