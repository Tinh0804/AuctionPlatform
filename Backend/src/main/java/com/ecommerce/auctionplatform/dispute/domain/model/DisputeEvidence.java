package com.ecommerce.auctionplatform.dispute.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DisputeEvidence {
    UUID id;
    UUID disputeId;
    String fileUrl;
    Integer sortOrder;
    String description;
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();
}
