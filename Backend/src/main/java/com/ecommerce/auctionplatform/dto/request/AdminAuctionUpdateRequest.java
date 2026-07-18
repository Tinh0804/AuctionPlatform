package com.ecommerce.auctionplatform.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminAuctionUpdateRequest {
    String name;
    String description;
    String origin;
    String categoryId;
    String condition;
    String manufactureYear;
    BigDecimal startPrice;
    BigDecimal stepPrice;
    BigDecimal depositAmount;
    LocalDateTime startTime;
    LocalDateTime endTime;
}
