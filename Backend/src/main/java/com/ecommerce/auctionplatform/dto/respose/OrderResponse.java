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
    UserResponse buyer;
    UserResponse seller;
    BigDecimal totalAmount;
    OrderStatus status;
    String trackingCode;
    String shippingProvider;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
