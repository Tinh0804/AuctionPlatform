package com.ecommerce.auctionplatform.payment.presentation.dto.response;

public record PaymentCallbackResponse(
        String orderId,
        String transactionId,
        Long amount,
        String paymentStatus,
        String paymentMethod,
        String message,
        String paymentTime
) {
}
