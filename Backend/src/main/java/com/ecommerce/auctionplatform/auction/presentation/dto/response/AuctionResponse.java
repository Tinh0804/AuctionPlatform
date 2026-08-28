package com.ecommerce.auctionplatform.auction.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AuctionResponse(
        UUID id,
        String productName,
        String categoryName,
        String status,
        BigDecimal currentPrice,
        Integer bidCount,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String coverImage
) {
}
