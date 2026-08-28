package com.ecommerce.auctionplatform.payment.application.event;

import com.ecommerce.auctionplatform.shared.domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderPaymentSucceededEvent(UUID orderId, BigDecimal paidAmount, LocalDateTime occurredOn)
        implements DomainEvent {
    public OrderPaymentSucceededEvent(UUID orderId, BigDecimal paidAmount) {
        this(orderId, paidAmount, LocalDateTime.now());
    }
}
