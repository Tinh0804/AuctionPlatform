package com.ecommerce.auctionplatform.dispute.domain.model;
import com.ecommerce.auctionplatform.dispute.domain.enums.DisputeStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Dispute {
    UUID id;

    UUID orderId;

    UUID claimantId;

    String reason;

    String description;

    @Builder.Default
    DisputeStatus status = DisputeStatus.OPEN;

    UUID resolvedById;

    String resolution;

    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    LocalDateTime resolvedAt;

    public void resolve(UUID resolverId, String resolution) {
        if (status == DisputeStatus.RESOLVED || status == DisputeStatus.CLOSED) {
            throw new IllegalStateException("Dispute is already resolved");
        }
        this.status = DisputeStatus.RESOLVED;
        this.resolvedById = resolverId;
        this.resolution = resolution;
        this.resolvedAt = LocalDateTime.now();
    }
}
