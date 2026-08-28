package com.ecommerce.auctionplatform.auction.infrastructure.persistence.entity;

import com.ecommerce.auctionplatform.auction.domain.enums.*;
import com.ecommerce.auctionplatform.auction.domain.model.*;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bids")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BidEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "user_id", nullable = false)
    UUID userId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    AuctionEntity auction;

    @Column(name = "bid_amount", nullable = false, precision = 18, scale = 2)
    BigDecimal bidAmount;

    @Builder.Default
    @Column(name = "bid_time", nullable = false, updatable = false)
    LocalDateTime bidTime = LocalDateTime.now();

    @Builder.Default
    @Column(name = "is_winning")
    Boolean isWinning = false;

    @Builder.Default
    @Column(name = "triggered_extend")
    Boolean triggeredExtend = false;

    @Column(name = "new_end_time")
    LocalDateTime newEndTime;
}
