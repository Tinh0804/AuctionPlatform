package com.ecommerce.auctionplatform.auction.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BidResponse(
        UUID id,
        BigDecimal bidAmount,
        String bidderName,
        UUID bidderId,
        LocalDateTime bidTime,
        Boolean isWinning
) {
}
