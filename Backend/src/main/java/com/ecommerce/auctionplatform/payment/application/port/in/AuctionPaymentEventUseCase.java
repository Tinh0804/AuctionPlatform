package com.ecommerce.auctionplatform.payment.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public interface AuctionPaymentEventUseCase {
    void reserveDeposit(UUID userId, UUID auctionId, BigDecimal amount);

    void forfeitDeposit(UUID userId, UUID auctionId, BigDecimal amount);

    void refundLoserDeposits(UUID auctionId, UUID winnerId);
}
