package com.ecommerce.auctionplatform.payment.application.dto.response;

import com.ecommerce.auctionplatform.payment.domain.enums.TransactionStatus;
import com.ecommerce.auctionplatform.payment.domain.enums.TransactionType;
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
