package com.ecommerce.auctionplatform.notification.domain.model;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {
    UUID id;

    UUID userId;

    String type;

    String title;

    String content;

    String referenceType;

    UUID referenceId;

    @Builder.Default
    Boolean isRead = false;

    LocalDateTime readAt;

    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    public void markAsRead() {
        if (!Boolean.TRUE.equals(isRead)) {
            isRead = true;
            readAt = LocalDateTime.now();
        }
    }
}
