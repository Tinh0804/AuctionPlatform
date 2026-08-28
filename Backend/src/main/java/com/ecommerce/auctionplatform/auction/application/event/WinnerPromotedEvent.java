package com.ecommerce.auctionplatform.auction.application.event;

import com.ecommerce.auctionplatform.shared.domain.event.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record WinnerPromotedEvent(
        UUID auctionId,
        UUID newWinnerId,
        LocalDateTime paymentDeadline,
        LocalDateTime occurredOn
) implements DomainEvent {
    public WinnerPromotedEvent(UUID auctionId, UUID newWinnerId, LocalDateTime paymentDeadline) {
        this(auctionId, newWinnerId, paymentDeadline, LocalDateTime.now());
    }
}
