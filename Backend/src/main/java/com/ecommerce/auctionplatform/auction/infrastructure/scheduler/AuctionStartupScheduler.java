package com.ecommerce.auctionplatform.auction.infrastructure.scheduler;

import com.ecommerce.auctionplatform.auction.application.port.in.AuctionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionStartupScheduler {
    private final AuctionUseCase auctionUseCase;

    @EventListener(ApplicationReadyEvent.class)
    public void processStuckAuctionsAfterStartup() {
        try {
            auctionUseCase.processAllStuckEntities();
        } catch (RuntimeException exception) {
            log.error("Unable to process stuck auctions after startup", exception);
        }
    }
}
