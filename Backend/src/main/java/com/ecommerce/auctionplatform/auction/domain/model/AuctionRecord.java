package com.ecommerce.auctionplatform.auction.domain.model;

import com.ecommerce.auctionplatform.auction.domain.enums.AuctionRecordStatus;
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
public class AuctionRecord {
            UUID id;

            Auction auction;

        UUID userId;

            Bid bid;

        Integer winningRank;

        BigDecimal finalPrice;

    @Builder.Default
                AuctionRecordStatus status = AuctionRecordStatus.PENDING_PAYMENT;

        LocalDateTime expiryTime;

    @Builder.Default
        LocalDateTime createdAt = LocalDateTime.now();

    public void cancel() {
        status = AuctionRecordStatus.CANCELLED;
    }

    public void promote(LocalDateTime paymentDeadline) {
        status = AuctionRecordStatus.PENDING_PAYMENT;
        expiryTime = paymentDeadline;
    }

    public void markWon() {
        status = AuctionRecordStatus.WIN;
    }
}
