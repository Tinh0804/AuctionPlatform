package com.ecommerce.auctionplatform.payment.application.port.in;

import java.util.UUID;

public interface OrderCreationUseCase {
    void createForWinner(UUID auctionId, UUID winnerId);
}
