package com.ecommerce.auctionplatform.notification.application.port.in;

import com.ecommerce.auctionplatform.notification.application.dto.response.NotificationResponse;
import java.util.List;
import java.util.UUID;

/**
 * Port/In – Use case interface for Notification domain.
 */
public interface NotificationUseCase {

    List<NotificationResponse> getMyNotifications();

    void markAsRead(UUID notificationId);

    void sendNotification(UUID userId, String type, String title, String content, String referenceType, UUID referenceId);
}
