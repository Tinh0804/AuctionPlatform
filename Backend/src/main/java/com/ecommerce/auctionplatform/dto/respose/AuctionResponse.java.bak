package com.ecommerce.auctionplatform.dto.respose;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuctionResponse {
    UUID id;
    String productName;
    String categoryName;
    String status;
    BigDecimal currentPrice;
    Integer bidCount;
    LocalDateTime startTime;
    LocalDateTime endTime;
    String coverImage;
}
