package com.ecommerce.auctionplatform.auction.application.dto.command;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUpdateAuctionCommand(
        String name,
        String description,
        String origin,
        UUID categoryId,
        String condition,
        String manufactureYear,
        BigDecimal startPrice,
        BigDecimal stepPrice,
        BigDecimal depositAmount,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
