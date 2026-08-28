package com.ecommerce.auctionplatform.auction.application.event;

import com.ecommerce.auctionplatform.shared.domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BidPlacedEvent(UUID auctionId, UUID bidderId, BigDecimal amount, LocalDateTime occurredOn)
        implements DomainEvent {
    public BidPlacedEvent(UUID auctionId, UUID bidderId, BigDecimal amount) {
        this(auctionId, bidderId, amount, LocalDateTime.now());
    }
}
