package com.ecommerce.auctionplatform.payment.application.dto.response;

import com.ecommerce.auctionplatform.payment.domain.enums.WalletStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletSnapshot(
        UUID id,
        BigDecimal availableBalance,
        BigDecimal frozenBalance,
        boolean hasPin,
        WalletStatus status
) {
}
