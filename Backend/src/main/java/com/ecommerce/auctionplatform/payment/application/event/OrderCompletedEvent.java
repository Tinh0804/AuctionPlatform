package com.ecommerce.auctionplatform.payment.application.event;

import com.ecommerce.auctionplatform.shared.domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCompletedEvent(
        UUID orderId,
        UUID sellerId,
        int rating,
        BigDecimal netAmount,
        LocalDateTime occurredOn
) implements DomainEvent {
    public OrderCompletedEvent(UUID orderId, UUID sellerId, int rating, BigDecimal netAmount) {
        this(orderId, sellerId, rating, netAmount, LocalDateTime.now());
    }
}
