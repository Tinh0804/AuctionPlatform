package com.ecommerce.auctionplatform.notification.application.dto.command;

import java.util.UUID;

public record AdminSendNotificationCommand(
        UUID userId,
        String roleName,
        String title,
        String content,
        String type
) {
}
