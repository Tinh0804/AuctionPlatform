package com.ecommerce.auctionplatform.shared.domain.event;

import java.time.LocalDateTime;

/**
 * Marker interface for all Domain Events.
 * Domain events represent something that happened in the domain.
 */
public interface DomainEvent {
    LocalDateTime occurredOn();
}
