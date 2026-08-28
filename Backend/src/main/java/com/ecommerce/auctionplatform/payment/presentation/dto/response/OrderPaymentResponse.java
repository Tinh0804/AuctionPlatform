package com.ecommerce.auctionplatform.payment.presentation.dto.response;

public record OrderPaymentResponse(
        String status,
        String paymentUrl,
        OrderResponse order
) {
}
