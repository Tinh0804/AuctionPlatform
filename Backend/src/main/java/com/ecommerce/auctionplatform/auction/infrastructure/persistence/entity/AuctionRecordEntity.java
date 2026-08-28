package com.ecommerce.auctionplatform.auction.infrastructure.persistence.entity;

import com.ecommerce.auctionplatform.auction.domain.enums.*;
import com.ecommerce.auctionplatform.auction.domain.model.*;

import com.ecommerce.auctionplatform.auction.domain.enums.AuctionRecordStatus;
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
public class AuctionRecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    AuctionEntity auction;

    @Column(name = "user_id")
    UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bid_id")
    BidEntity bid;

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
