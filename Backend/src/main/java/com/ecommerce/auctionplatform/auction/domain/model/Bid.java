package com.ecommerce.auctionplatform.auction.domain.model;

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
public class Bid {
            UUID id;

        UUID userId;

            Auction auction;

        BigDecimal bidAmount;

    @Builder.Default
        LocalDateTime bidTime = LocalDateTime.now();

    @Builder.Default
        Boolean isWinning = false;

    @Builder.Default
        Boolean triggeredExtend = false;

        LocalDateTime newEndTime;
}
