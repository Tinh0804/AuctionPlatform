package com.ecommerce.auctionplatform.dispute.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface DisputeContextPort {
    Optional<DisputeOrderView> findOrder(UUID orderId);

    Optional<String> findUserName(UUID userId);

    Optional<UUID> findAdminId();

    void markOrderDisputed(UUID orderId);

    void settleBuyerWin(DisputeOrderView order, UUID disputeId);

    void settleSellerWin(DisputeOrderView order, UUID disputeId, UUID adminId);

    void decreaseReputation(UUID userId, int points, String reason, UUID disputeId);
}
