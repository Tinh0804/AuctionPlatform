package com.ecommerce.auctionplatform.auction.application.event;

import com.ecommerce.auctionplatform.shared.domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AuctionEndedEvent(UUID auctionId, UUID winnerId, BigDecimal finalPrice, LocalDateTime occurredOn)
        implements DomainEvent {
    public AuctionEndedEvent(UUID auctionId, UUID winnerId, BigDecimal finalPrice) {
        this(auctionId, winnerId, finalPrice, LocalDateTime.now());
    }
}
