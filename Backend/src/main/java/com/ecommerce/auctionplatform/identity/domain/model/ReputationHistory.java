package com.ecommerce.auctionplatform.identity.domain.model;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReputationHistory {
    UUID id;

    UUID userId;

    Integer scoreChange;

    String reason;

    UUID orderId;

    UUID disputeId;

    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();
}
