package com.ecommerce.auctionplatform.payment.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public record AuctionRegistrationView(
        UUID id,
        UUID auctionId,
        UUID userId,
        BigDecimal depositAmount,
        boolean paid
) {
}
