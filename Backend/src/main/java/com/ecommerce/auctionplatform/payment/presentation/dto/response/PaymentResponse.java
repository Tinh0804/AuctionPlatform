package com.ecommerce.auctionplatform.payment.presentation.dto.response;

public record PaymentResponse(
        String status,
        String message,
        String paymentUrl,
        String orderId,
        String transactionId,
        Double amount,
        String paymentMethod
) {
}
