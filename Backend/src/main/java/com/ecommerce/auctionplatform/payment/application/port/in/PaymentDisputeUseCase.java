package com.ecommerce.auctionplatform.payment.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/** Payment-owned dispute operations. Settlement remains inside the payment boundary. */
public interface PaymentDisputeUseCase {
    Optional<DisputeOrderSnapshot> findOrder(UUID orderId);

    void markOrderDisputed(UUID orderId);

    void settleBuyerWin(UUID orderId, UUID disputeId);

    void settleSellerWin(UUID orderId, UUID disputeId, UUID adminId);

    record DisputeOrderSnapshot(
            UUID id,
            UUID auctionRecordId,
            UUID buyerId,
            UUID sellerId,
            BigDecimal amount,
            boolean eligibleForDispute,
            String trackingCode,
            LocalDateTime updatedAt,
            String productName,
            String productImageUrl
    ) {
    }
}
