package com.ecommerce.auctionplatform.notification.infrastructure.messaging;

import com.ecommerce.auctionplatform.notification.application.dto.response.NotificationResponse;
import com.ecommerce.auctionplatform.notification.application.port.out.NotificationSenderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationSender implements NotificationSenderPort {
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void send(UUID userId, NotificationResponse notification) {
        try {
            messagingTemplate.convertAndSend("/topic/notification/" + userId, notification);
        } catch (RuntimeException exception) {
            log.warn("Failed to push notification to user {}: {}", userId, exception.getMessage());
        }
    }
}
