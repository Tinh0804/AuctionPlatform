package com.ecommerce.auctionplatform.dispute.infrastructure.persistence.entity;

import com.ecommerce.auctionplatform.dispute.domain.enums.DisputeStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "disputes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "claimant_id", nullable = false)
    private UUID claimantId;

    @Column(nullable = false, length = 255)
    private String reason;
    @Column(length = 2000)
    private String description;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "dispute_status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private DisputeStatus status = DisputeStatus.OPEN;

    @Column(name = "resolved_by")
    private UUID resolvedById;

    @Column(length = 1000)
    private String resolution;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
