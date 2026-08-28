package com.ecommerce.auctionplatform.auction.application.port.out;

import java.time.LocalDateTime;

public interface AuctionSchedulePort {
    void scheduleActivation(String auctionId, LocalDateTime startTime);

    void scheduleClosure(String auctionId, LocalDateTime endTime);

    void schedulePaymentExpiry(String recordId, LocalDateTime expiryTime);
}
