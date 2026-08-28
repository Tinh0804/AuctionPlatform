package com.ecommerce.auctionplatform.payment.infrastructure.persistence.entity;

import com.ecommerce.auctionplatform.payment.domain.enums.TransactionStatus;
import com.ecommerce.auctionplatform.payment.domain.enums.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;

    @Column(name = "related_wallet_id")
    private UUID relatedWalletId;

    @Column(nullable = false, columnDefinition = "transaction_type")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private TransactionType type;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "transaction_status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "gateway_provider", length = 20)
    private String gatewayProvider;
    @Column(name = "gateway_tx_id", length = 255)
    private String gatewayTxId;
    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;
    @Column(name = "reference_type", length = 20)
    private String referenceType;
    @Column(name = "reference_id")
    private UUID referenceId;
    @Column(length = 500)
    private String note;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
