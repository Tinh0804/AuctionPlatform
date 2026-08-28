package com.ecommerce.auctionplatform.notification.application.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    UUID id;
    String type;
    String title;
    String message;       // maps from entity "content"
    String referenceType;
    UUID referenceId;
    Boolean isRead;
    LocalDateTime createdAt;
}
