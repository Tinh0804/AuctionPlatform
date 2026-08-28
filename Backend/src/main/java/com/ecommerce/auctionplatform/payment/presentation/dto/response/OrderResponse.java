package com.ecommerce.auctionplatform.payment.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID auctionId,
        String productName,
        String productImageUrl,
        UUID sellerId,
        String sellerName,
        UUID buyerId,
        String buyerName,
        BigDecimal totalAmount,
        BigDecimal depositAmount,
        String status,
        String trackingCode,
        String shippingProvider,
        Integer ratingScore,
        String reviewContent,
        LocalDateTime reviewDate,
        LocalDateTime paymentDeadline,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
