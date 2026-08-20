package com.ecommerce.auctionplatform.dto.respose;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
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
