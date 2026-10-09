package com.ecommerce.backend.service.impl;

import com.ecommerce.backend.dto.CreateOrderRequest;
import com.ecommerce.backend.dto.OrderItemResponse;
import com.ecommerce.backend.dto.OrderResponse;
import com.ecommerce.backend.entity.*;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.*;
import com.ecommerce.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(String email, CreateOrderRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + email));

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot create order from an empty cart");
        }

        Order order = Order.builder()
                .user(user)
                .shippingAddress(request.getShippingAddress())
                .status(Order.OrderStatus.CONFIRMED)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (!product.isActive()) {
                throw new IllegalArgumentException("Product is not available: " + product.getName());
            }
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock for product: " + product.getName());
            }

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(subtotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .subtotal(subtotal)
                    .build();
            orderItems.add(orderItem);

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        order.setTotalAmount(total);
        order.setItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        Payment.PaymentMethod paymentMethod = Payment.PaymentMethod.COD;
        if (request.getPaymentMethod() != null && !request.getPaymentMethod().isBlank()) {
            paymentMethod = Payment.PaymentMethod.valueOf(request.getPaymentMethod().trim().toUpperCase(Locale.ROOT));
        }

        Payment payment = Payment.builder()
                .order(savedOrder)
                .amount(total)
                .paymentMethod(paymentMethod)
                .paymentStatus(Payment.PaymentStatus.SUCCESS)
                .transactionId("SIM-" + UUID.randomUUID())
                .build();
        paymentRepository.save(payment);

        cart.getItems().clear();
        cartRepository.save(cart);

        return toResponse(savedOrder);
    }

    @Override
    public List<OrderResponse> getOrdersForCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return orderRepository.findByUser(user, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream().map(this::toResponse).toList();
    }

    @Override
    public OrderResponse getOrderById(String email, Long orderId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not allowed to view this order");
        }
        return toResponse(order);
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(String email, Long orderId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not allowed to cancel this order");
        }
        order.setStatus(Order.OrderStatus.CANCELLED);
        return toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        order.setStatus(Order.OrderStatus.valueOf(status.trim().toUpperCase(Locale.ROOT)));
        return toResponse(orderRepository.save(order));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = new ArrayList<>();
        for (OrderItem orderItem : order.getItems()) {
            OrderItemResponse itemResponse = new OrderItemResponse();
            itemResponse.setId(orderItem.getId());
            itemResponse.setProductId(orderItem.getProduct().getId());
            itemResponse.setProductName(orderItem.getProduct().getName());
            itemResponse.setProductImageUrl(orderItem.getProduct().getImageUrl());
            itemResponse.setQuantity(orderItem.getQuantity());
            itemResponse.setPrice(orderItem.getPrice());
            itemResponse.setSubtotal(orderItem.getSubtotal());
            items.add(itemResponse);
        }

        Payment payment = paymentRepository.findByOrder(order).orElse(null);

        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUser().getId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus().name());
        response.setPaymentStatus(payment != null ? payment.getPaymentStatus().name() : Payment.PaymentStatus.PENDING.name());
        response.setShippingAddress(order.getShippingAddress());
        response.setCreatedAt(order.getCreatedAt());
        response.setItems(items);
        return response;
    }
}
