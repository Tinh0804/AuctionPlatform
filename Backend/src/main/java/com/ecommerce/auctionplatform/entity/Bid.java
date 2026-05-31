package com.ecommerce.auctionplatform.entity;

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
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "auction_id", nullable = false)
    Auction auction;

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
