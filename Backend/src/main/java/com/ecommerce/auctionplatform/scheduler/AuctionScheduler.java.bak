package com.ecommerce.auctionplatform.scheduler;

import com.ecommerce.auctionplatform.service.AuctionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuctionScheduler {

    AuctionService auctionService;

    @EventListener(ApplicationReadyEvent.class) //Sự kiện này kích hoạt khi server start
    public void onApplicationReady() {
        log.info("Application started. Running fallback check for stuck auctions and orders...");
        try {
            auctionService.processAllStuckEntities();
            log.info("Fallback check completed successfully.");
        } catch (Exception e) {
            log.error("Error during startup fallback check", e);
        }
    }
}
