package com.ecommerce.auctionplatform.payment.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        String type,
        BigDecimal amount,
        String status,
        String note,
        LocalDateTime createdAt
) {
}
