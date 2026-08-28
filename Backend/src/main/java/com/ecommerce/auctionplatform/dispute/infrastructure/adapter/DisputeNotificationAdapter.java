package com.ecommerce.auctionplatform.dispute.infrastructure.adapter;

import com.ecommerce.auctionplatform.dispute.application.port.out.DisputeNotificationPort;
import com.ecommerce.auctionplatform.notification.application.port.in.NotificationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DisputeNotificationAdapter implements DisputeNotificationPort {
    private final NotificationUseCase notificationUseCase;

    @Override
    public void notify(UUID userId, String type, String title, String message, UUID disputeId) {
        notificationUseCase.sendNotification(userId, type, title, message, "DISPUTE", disputeId);
    }
}
