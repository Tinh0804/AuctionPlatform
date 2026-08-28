package com.ecommerce.auctionplatform.notification.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String type,
        String title,
        String message,
        String referenceType,
        UUID referenceId,
        Boolean isRead,
        LocalDateTime createdAt
) {
}
