package com.ecommerce.auctionplatform.payment.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuctionQueryPort {
    Optional<AuctionRecordView> findRecord(UUID auctionRecordId);

    Optional<AuctionRecordView> findRecord(UUID auctionId, UUID userId);

    List<AuctionRegistrationView> findRegistrations(UUID auctionId);

    Optional<AuctionRegistrationView> findRegistration(UUID auctionId, UUID userId);

    void markRegistrationRefunded(UUID registrationId);

    void markRecordWon(UUID auctionRecordId);
}
