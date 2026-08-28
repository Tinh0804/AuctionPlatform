package com.ecommerce.auctionplatform.dispute.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DisputeResponse(
        UUID id,
        UUID orderId,
        String productName,
        String productImageUrl,
        String claimantName,
        String sellerName,
        String buyerName,
        BigDecimal orderAmount,
        String reason,
        String description,
        List<EvidenceResponse> evidences,
        String status,
        String resolvedByName,
        String resolution,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {
}
