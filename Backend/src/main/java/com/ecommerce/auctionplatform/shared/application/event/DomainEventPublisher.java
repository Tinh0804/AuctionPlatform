package com.ecommerce.auctionplatform.shared.application.event;

import com.ecommerce.auctionplatform.shared.domain.event.DomainEvent;

/**
 * Port OUT – Application-level interface for publishing domain events.
 * Infrastructure layer provides the implementation (Spring Application Events).
 */
public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
