package com.ecommerce.auctionplatform.entity;

import com.ecommerce.auctionplatform.entity.enums.TransactionType;
import com.ecommerce.auctionplatform.entity.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    Wallet wallet;

    @ManyToOne
    @JoinColumn(name = "related_wallet_id")
    Wallet relatedWallet;

    @Column(name = "type", nullable = false, columnDefinition = "transaction_type")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    TransactionType type;

    @Column(nullable = false, precision = 18, scale = 2)
    BigDecimal amount;

    @Builder.Default
    @Column(name = "status", nullable = false, columnDefinition = "transaction_status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "gateway_provider", length = 20)
    String gatewayProvider;

    @Column(name = "gateway_tx_id", length = 255)
    String gatewayTxId;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    String gatewayResponse;

    @Column(name = "expired_at")
    LocalDateTime expiredAt;

    @Column(name = "reference_type", length = 20)
    String referenceType;

    @Column(name = "reference_id")
    UUID referenceId;

    @Column(length = 500)
    String note;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt = LocalDateTime.now();
}
