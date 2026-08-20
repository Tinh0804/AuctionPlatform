package com.ecommerce.auctionplatform.shared.infrastructure.event;

import com.ecommerce.auctionplatform.shared.application.event.DomainEventPublisher;
import com.ecommerce.auctionplatform.shared.domain.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Infrastructure adapter that implements DomainEventPublisher
 * using Spring's ApplicationEventPublisher.
 */
@Component
@RequiredArgsConstructor
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
