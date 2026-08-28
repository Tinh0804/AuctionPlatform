package com.ecommerce.auctionplatform.auction.application.event;

import com.ecommerce.auctionplatform.shared.domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DepositReservationRequestedEvent(UUID auctionId, UUID userId, BigDecimal amount, LocalDateTime occurredOn)
        implements DomainEvent {
    public DepositReservationRequestedEvent(UUID auctionId, UUID userId, BigDecimal amount) {
        this(auctionId, userId, amount, LocalDateTime.now());
    }
}
