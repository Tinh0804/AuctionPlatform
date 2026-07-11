package com.ecommerce.auctionplatform.dto.respose;

import com.ecommerce.auctionplatform.entity.enums.TransactionStatus;
import com.ecommerce.auctionplatform.entity.enums.TransactionType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionResponse {
    UUID id;
    TransactionType type;
    BigDecimal amount;
    TransactionStatus status;
    String note;
    LocalDateTime createdAt;
}
