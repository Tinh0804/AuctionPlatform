package com.ecommerce.auctionplatform.notification.application.port.out;

import com.ecommerce.auctionplatform.notification.application.dto.response.NotificationResponse;

import java.util.UUID;

/**
 * Port/Out – Abstraction for real-time notification/push delivery.
 * Implementations can use WebSocket (STOMP), Firebase FCM, email, etc.
 */
public interface NotificationSenderPort {

    /**
     * Send a real-time notification to a specific user.
     * @param userId    the recipient
     * @param type      notification type (e.g. "BID_PLACED", "AUCTION_CLOSED")
     * @param title     notification title
     * @param content   notification body
     * @param referenceId optional ID of the related entity (auction, order, etc.)
     */
    void send(UUID userId, NotificationResponse notification);
}
