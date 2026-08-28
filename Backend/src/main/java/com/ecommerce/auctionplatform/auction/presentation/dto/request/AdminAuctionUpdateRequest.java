package com.ecommerce.auctionplatform.auction.presentation.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminAuctionUpdateRequest(
        String name,
        String description,
        String origin,
        String categoryId,
        String condition,
        String manufactureYear,
        BigDecimal startPrice,
        BigDecimal stepPrice,
        BigDecimal depositAmount,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
