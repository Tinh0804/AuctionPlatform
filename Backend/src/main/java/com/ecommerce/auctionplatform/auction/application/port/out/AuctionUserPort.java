package com.ecommerce.auctionplatform.auction.application.port.out;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface AuctionUserPort {
    Optional<AuctionUserView> findById(UUID userId);
    Map<UUID, AuctionUserView> findByIds(Iterable<UUID> userIds);
}

