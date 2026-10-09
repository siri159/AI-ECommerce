package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.PaymentInitiateRequest;
import com.ecommerce.backend.dto.PaymentResponse;
import com.ecommerce.backend.service.PaymentService;
import com.ecommerce.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/payments/initiate")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(Principal principal,
                                                                        @Valid @RequestBody PaymentInitiateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment session created successfully",
                        paymentService.initiatePayment(principal.getName(), request)));
    }

    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentStatus(Principal principal, @PathVariable Long paymentId) {
        return ResponseEntity.ok(ApiResponse.success("Payment status retrieved successfully",
                paymentService.getPaymentStatus(principal.getName(), paymentId)));
    }

    @PostMapping("/payments/{paymentId}/complete")
    public ResponseEntity<ApiResponse<PaymentResponse>> completePayment(Principal principal, @PathVariable Long paymentId) {
        return ResponseEntity.ok(ApiResponse.success("Payment successful",
                paymentService.completePayment(principal.getName(), paymentId)));
    }

    @PostMapping("/payments/{paymentId}/fail")
    public ResponseEntity<ApiResponse<PaymentResponse>> failPayment(Principal principal, @PathVariable Long paymentId) {
        return ResponseEntity.ok(ApiResponse.success("Payment failed",
                paymentService.failPayment(principal.getName(), paymentId)));
    }

    @PostMapping("/payments/{paymentId}/cancel")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(Principal principal, @PathVariable Long paymentId) {
        return ResponseEntity.ok(ApiResponse.success("Payment cancelled",
                paymentService.cancelPayment(principal.getName(), paymentId)));
    }
}
