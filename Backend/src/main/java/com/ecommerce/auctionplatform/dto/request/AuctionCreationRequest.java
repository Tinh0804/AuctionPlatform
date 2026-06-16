package com.ecommerce.auctionplatform.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuctionCreationRequest {
    String name;
    String description;
    String origin;
    String categoryId;
    String condition = "NEW";
    BigDecimal startPrice;
    BigDecimal stepPrice;
    BigDecimal depositAmount = BigDecimal.ZERO;
    LocalDateTime startTime;
    LocalDateTime endTime;
    BigDecimal reservePrice;
    BigDecimal buyNowPrice;
    MultipartFile[] files;
}
