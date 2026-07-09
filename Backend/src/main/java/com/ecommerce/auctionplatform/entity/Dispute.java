package com.ecommerce.auctionplatform.entity;

import com.ecommerce.auctionplatform.entity.enums.DisputeStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
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
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Dispute {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    @ManyToOne(optional = false)
    @JoinColumn(name = "claimant_id", nullable = false)
    User claimant;

    @Column(nullable = false, length = 255)
    String reason;

    @Column(length = 2000)
    String description;

    @Builder.Default
    @Column(name = "status", nullable = false, columnDefinition = "dispute_status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    DisputeStatus status = DisputeStatus.OPEN;

    @ManyToOne
    @JoinColumn(name = "resolved_by")
    User resolvedBy;

    @Column(length = 1000)
    String resolution;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    LocalDateTime resolvedAt;
}
