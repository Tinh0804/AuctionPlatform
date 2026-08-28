package com.ecommerce.auctionplatform.auction.application.event;

import com.ecommerce.auctionplatform.shared.domain.event.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuctionFailedEvent(UUID auctionId, String reason, LocalDateTime occurredOn) implements DomainEvent {
    public AuctionFailedEvent(UUID auctionId, String reason) {
        this(auctionId, reason, LocalDateTime.now());
    }
}
