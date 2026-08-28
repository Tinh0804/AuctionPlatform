package com.ecommerce.auctionplatform.auction.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Public auction contract used by payment without exposing auction aggregates. */
public interface AuctionSettlementUseCase {
    Optional<AuctionRecordSummary> findRecord(UUID auctionRecordId);

    Optional<AuctionRecordSummary> findRecord(UUID auctionId, UUID userId);

    List<AuctionRegistrationSummary> findRegistrations(UUID auctionId);

    Optional<AuctionRegistrationSummary> findRegistration(UUID auctionId, UUID userId);

    void markRegistrationRefunded(UUID registrationId);

    void markRecordWon(UUID auctionRecordId);

    record AuctionRecordSummary(
            UUID id,
            UUID auctionId,
            UUID sellerId,
            UUID productId,
            String productName,
            String productImageUrl,
            BigDecimal depositAmount,
            BigDecimal platformFee,
            BigDecimal finalPrice,
            LocalDateTime paymentDeadline
    ) {
    }

    record AuctionRegistrationSummary(
            UUID id,
            UUID auctionId,
            UUID userId,
            BigDecimal depositAmount,
            boolean paid
    ) {
    }
}
