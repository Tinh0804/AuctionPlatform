package com.ecommerce.auctionplatform.auction.application.event;

import com.ecommerce.auctionplatform.shared.domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DepositForfeitedEvent(UUID auctionId, UUID userId, BigDecimal depositAmount, LocalDateTime occurredOn)
        implements DomainEvent {
    public DepositForfeitedEvent(UUID auctionId, UUID userId, BigDecimal depositAmount) {
        this(auctionId, userId, depositAmount, LocalDateTime.now());
    }
}
