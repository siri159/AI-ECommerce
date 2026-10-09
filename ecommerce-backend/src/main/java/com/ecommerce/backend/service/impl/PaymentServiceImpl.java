package com.ecommerce.backend.service.impl;

import com.ecommerce.backend.dto.PaymentInitiateRequest;
import com.ecommerce.backend.dto.PaymentResponse;
import com.ecommerce.backend.entity.*;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.*;
import com.ecommerce.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public PaymentResponse initiatePayment(String email, PaymentInitiateRequest request) {
        User user = findUser(email);
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + email));

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout an empty cart");
        }

        Payment.PaymentMethod paymentMethod = parsePaymentMethod(request.getPaymentMethod());
        BigDecimal amount = getCartTotal(cart);

        Payment payment = Payment.builder()
                .amount(amount)
                .paymentMethod(paymentMethod)
                .paymentStatus(Payment.PaymentStatus.PENDING)
                .transactionId("TEST-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase())
                .shippingAddress(request.getShippingAddress())
                .build();

        return toResponse(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponse getPaymentStatus(String email, Long paymentId) {
        findUser(email);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        return toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse completePayment(String email, Long paymentId) {
        User user = findUser(email);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + email));

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot create an order from an empty cart");
        }

        if (payment.getPaymentStatus() == Payment.PaymentStatus.SUCCESS) {
            return toResponse(payment);
        }

        Order order = createOrderFromCart(user, cart, payment.getShippingAddress(), payment.getPaymentMethod());
        payment.setOrder(order);
        payment.setPaymentStatus(Payment.PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        cart.getItems().clear();
        cartRepository.save(cart);

        return toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse failPayment(String email, Long paymentId) {
        findUser(email);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
        return toResponse(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentResponse cancelPayment(String email, Long paymentId) {
        findUser(email);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        payment.setPaymentStatus(Payment.PaymentStatus.CANCELLED);
        return toResponse(paymentRepository.save(payment));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private Payment.PaymentMethod parsePaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return Payment.PaymentMethod.CARD;
        }
        try {
            return Payment.PaymentMethod.valueOf(paymentMethod.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        }
    }

    private BigDecimal getCartTotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Order createOrderFromCart(User user, Cart cart, String shippingAddress, Payment.PaymentMethod paymentMethod) {
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot create order from an empty cart");
        }

        Order order = Order.builder()
                .user(user)
                .shippingAddress(shippingAddress)
                .status(Order.OrderStatus.CONFIRMED)
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

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

        order.setItems(orderItems);
        order.setTotalAmount(total);
        return orderRepository.save(order);
    }

    private PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrder() != null ? payment.getOrder().getId() : null);
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod().name());
        response.setPaymentStatus(payment.getPaymentStatus().name());
        response.setShippingAddress(payment.getShippingAddress());
        response.setTransactionId(payment.getTransactionId());
        response.setMessage("Payment " + payment.getPaymentStatus().name().toLowerCase());
        return response;
    }
}
