package com.ecommerce.auctionplatform.identity.presentation.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record WalletResponse(
        UUID id,
        BigDecimal availableBalance,
        BigDecimal frozenBalance,
        boolean hasPin,
        String status
) {
}
