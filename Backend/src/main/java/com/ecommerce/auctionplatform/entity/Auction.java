package com.ecommerce.auctionplatform.entity;

import com.ecommerce.auctionplatform.entity.enums.AuctionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "auctions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Auction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "product_id")
    Product product;

    @Column(name = "start_price", nullable = false, precision = 18, scale = 2)
    BigDecimal startPrice;

    @Column(name = "current_price", nullable = false, precision = 18, scale = 2)
    BigDecimal currentPrice;

    @Column(name = "step_price", nullable = false, precision = 18, scale = 2)
    BigDecimal stepPrice;

    @Column(name = "deposit_amount", nullable = false, precision = 18, scale = 2)
    BigDecimal depositAmount;

    @Column(name = "platform_fee", precision = 18, scale = 2)
    BigDecimal platformFee;

    @Column(name = "cancellation_fee", precision = 18, scale = 2)
    BigDecimal cancellationFee;

    @Column(name = "start_time", nullable = false)
    LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    LocalDateTime endTime;

    @Builder.Default
    @Column(name = "status", nullable = false, columnDefinition = "auction_status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    AuctionStatus status = AuctionStatus.PENDING;

    @Column(length = 2000)
    String description;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
