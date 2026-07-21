package com.ecommerce.auctionplatform.dto.respose;

import com.ecommerce.auctionplatform.entity.enums.OrderStatus;
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
public class OrderResponse {
    UUID id;
    UUID auctionId;
    String productName;
    String productImageUrl;
    UUID sellerId;
    String sellerName;
    UUID buyerId;
    String buyerName;
    BigDecimal totalAmount;
    BigDecimal depositAmount;
    OrderStatus status;
    String trackingCode;
    String shippingProvider;
    Integer ratingScore;
    String reviewContent;
    LocalDateTime reviewDate;
    LocalDateTime paymentDeadline;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
