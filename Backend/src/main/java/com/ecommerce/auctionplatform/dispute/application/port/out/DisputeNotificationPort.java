package com.ecommerce.auctionplatform.dispute.application.port.out;

import java.util.UUID;

public interface DisputeNotificationPort {
    void notify(UUID userId, String type, String title, String message, UUID disputeId);
}
