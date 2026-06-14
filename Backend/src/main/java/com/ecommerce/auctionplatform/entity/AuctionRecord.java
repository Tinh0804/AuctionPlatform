package com.ecommerce.auctionplatform.entity;

import com.ecommerce.auctionplatform.entity.enums.AuctionRecordStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "auction_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuctionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne
    @JoinColumn(name = "auction_id")
    Auction auction;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "bid_id")
    Bid bid;

    @Column(name = "winning_rank")
    Integer winningRank;

    @Column(name = "final_price", precision = 18, scale = 2)
    BigDecimal finalPrice;

    @Builder.Default
    @Column(name = "status", nullable = false, columnDefinition = "auction_record_status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    AuctionRecordStatus status = AuctionRecordStatus.PENDING_PAYMENT;

    @Column(name = "expiry_time", nullable = false)
    LocalDateTime expiryTime;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt = LocalDateTime.now();
}
