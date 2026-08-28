package com.ecommerce.auctionplatform.auction.domain.model;

import com.ecommerce.auctionplatform.auction.domain.enums.DepositStatus;
import com.ecommerce.auctionplatform.auction.domain.enums.RegistrationStatus;
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
public class AuctionRegistration {
            UUID id;

            Auction auction;

        UUID userId;

        BigDecimal depositAmount;

    @Builder.Default
                DepositStatus depositStatus = DepositStatus.PENDING;

    @Builder.Default
                RegistrationStatus registrationStatus = RegistrationStatus.PENDING;

    @Builder.Default
        LocalDateTime registeredAt = LocalDateTime.now();

        LocalDateTime approvedAt;

        String note;

    public void forfeitDeposit() {
        depositStatus = DepositStatus.FORFEITED;
    }

    public void refundDeposit() {
        depositStatus = DepositStatus.REFUNDED;
    }
}
