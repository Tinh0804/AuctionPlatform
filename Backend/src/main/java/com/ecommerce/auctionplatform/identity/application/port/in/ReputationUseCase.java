package com.ecommerce.auctionplatform.identity.application.port.in;

import java.util.UUID;

public interface ReputationUseCase {
    void changeForOrder(UUID userId, int scoreChange, String reason, UUID orderId);

    void decreaseForDispute(UUID userId, int points, String reason, UUID disputeId);

    void decreaseForAuction(UUID userId, int points, String reason, UUID auctionId);
}
