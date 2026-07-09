package com.ecommerce.auctionplatform.dto.respose;

import com.ecommerce.auctionplatform.entity.enums.DisputeStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DisputeResponse {
    UUID id;
    UUID orderId;
    String productName;
    String productImageUrl;
    String claimantName;
    String sellerName;
    String buyerName;
    BigDecimal orderAmount;
    String reason;
    String description;
    List<ImageResponse> evidences;
    DisputeStatus status;
    String resolvedByName;
    String resolution;
    LocalDateTime createdAt;
    LocalDateTime resolvedAt;
}
