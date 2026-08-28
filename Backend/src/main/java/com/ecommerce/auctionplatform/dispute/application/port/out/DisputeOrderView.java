package com.ecommerce.auctionplatform.dispute.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DisputeOrderView(
        UUID id,
        UUID auctionRecordId,
        UUID buyerId,
        UUID sellerId,
        BigDecimal amount,
        boolean eligibleForDispute,
        String trackingCode,
        LocalDateTime updatedAt,
        String productName,
        String productImageUrl
) {
}
