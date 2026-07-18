package com.ecommerce.auctionplatform.dto.respose;

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
public class AuctionDetailResponse {
    UUID id;
    String productName;
    String description;
    String status;
    BigDecimal startPrice;
    BigDecimal currentPrice;
    BigDecimal stepPrice;
    BigDecimal depositAmount;
    LocalDateTime startTime;
    LocalDateTime endTime;
    Boolean autoExtend;
    Integer extendMinutes;
    String sellerName;
    UUID sellerId;
    List<ImageResponse> productImages;
    String productCondition;
    String productManufactureYear;
    String productOrigin;
    Boolean hasCertificate;
    String provenanceFileUrl;
    String productDescription;
}
