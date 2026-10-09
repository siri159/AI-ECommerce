package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.PaymentInitiateRequest;
import com.ecommerce.backend.dto.PaymentResponse;

public interface PaymentService {
    PaymentResponse initiatePayment(String email, PaymentInitiateRequest request);
    PaymentResponse getPaymentStatus(String email, Long paymentId);
    PaymentResponse completePayment(String email, Long paymentId);
    PaymentResponse failPayment(String email, Long paymentId);
    PaymentResponse cancelPayment(String email, Long paymentId);
}
