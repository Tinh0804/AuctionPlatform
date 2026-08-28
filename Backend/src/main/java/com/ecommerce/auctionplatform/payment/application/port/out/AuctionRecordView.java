package com.ecommerce.auctionplatform.payment.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AuctionRecordView(
        UUID id,
        UUID auctionId,
        UUID sellerId,
        UUID productId,
        String productName,
        String productImageUrl,
        BigDecimal depositAmount,
        BigDecimal platformFee,
        BigDecimal finalPrice,
        LocalDateTime paymentDeadline
) {
}
