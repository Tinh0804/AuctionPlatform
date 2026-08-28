package com.ecommerce.auctionplatform.auction.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AuctionDetailResponse(
        UUID id,
        String productName,
        String categoryName,
        String productOrigin,
        String productCondition,
        String productManufactureYear,
        String description,
        String status,
        BigDecimal startPrice,
        BigDecimal currentPrice,
        BigDecimal stepPrice,
        BigDecimal depositAmount,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Boolean autoExtend,
        Integer extendMinutes,
        String sellerName,
        UUID sellerId,
        List<AuctionImageResponse> images
) {
}
