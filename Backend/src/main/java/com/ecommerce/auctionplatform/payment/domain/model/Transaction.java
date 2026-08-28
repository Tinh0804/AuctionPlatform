package com.ecommerce.auctionplatform.payment.domain.model;

import com.ecommerce.auctionplatform.payment.domain.enums.TransactionType;
import com.ecommerce.auctionplatform.payment.domain.enums.TransactionStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Transaction {
    public void markAsSuccess(String txId, String response) {
        if (txId != null) this.gatewayTxId = txId;
        if (response != null) this.gatewayResponse = response;
        this.status = TransactionStatus.SUCCESS;
    }
    public void markAsFailed(String response) {
        if (response != null) this.gatewayResponse = response;
        this.status = TransactionStatus.FAILED;
    }

    UUID id;

    UUID walletId;

    UUID relatedWalletId;

    TransactionType type;

    BigDecimal amount;

    @Builder.Default
    TransactionStatus status = TransactionStatus.PENDING;

    String gatewayProvider;

    String gatewayTxId;

    String gatewayResponse;

    LocalDateTime expiredAt;

    String referenceType;

    UUID referenceId;

    String note;

    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();
}
