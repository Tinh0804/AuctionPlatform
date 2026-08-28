package com.ecommerce.auctionplatform.auction.domain.repository;

import com.ecommerce.auctionplatform.auction.domain.model.AuctionRegistration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuctionRegistrationRepository {
    AuctionRegistration save(AuctionRegistration registration);
    Optional<AuctionRegistration> findByAuctionIdAndUserId(UUID auctionId, UUID userId);
    List<AuctionRegistration> findByAuctionId(UUID auctionId);
    Optional<AuctionRegistration> findById(UUID id);
}
