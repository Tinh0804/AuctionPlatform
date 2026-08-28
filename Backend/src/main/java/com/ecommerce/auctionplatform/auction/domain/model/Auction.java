package com.ecommerce.auctionplatform.auction.domain.model;

import com.ecommerce.auctionplatform.auction.domain.enums.AuctionStatus;
import com.ecommerce.auctionplatform.auction.domain.exception.AuctionEndedException;
import com.ecommerce.auctionplatform.auction.domain.exception.AuctionNotActiveException;
import com.ecommerce.auctionplatform.auction.domain.exception.BidTooLowException;
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
public class Auction {
            UUID id;

        UUID userId;

        UUID productId;

        BigDecimal startPrice;

        BigDecimal currentPrice;

        BigDecimal stepPrice;

        BigDecimal depositAmount;

        BigDecimal platformFee;

        BigDecimal cancellationFee;

        LocalDateTime startTime;

        LocalDateTime endTime;

    @Builder.Default
                AuctionStatus status = AuctionStatus.PENDING;

        String description;

    @Builder.Default
        Boolean autoExtend = false;

    @Builder.Default
        Integer extendMinutes = 0;

    @Builder.Default
        LocalDateTime createdAt = LocalDateTime.now();

        LocalDateTime updatedAt;

    // --- Domain Behaviors ---

    public void activate() {
        if (this.status != AuctionStatus.PENDING && this.status != AuctionStatus.APPROVED) {
            throw new IllegalStateException("Auction cannot be activated from status " + this.status);
        }
        this.status = AuctionStatus.ACTIVE;
    }

    public boolean canAcceptBid(BigDecimal bidAmount) {
        if (bidAmount == null || this.status != AuctionStatus.ACTIVE) {
            return false;
        }
        if (LocalDateTime.now().isBefore(this.startTime) || LocalDateTime.now().isAfter(this.endTime)) {
            return false;
        }
        BigDecimal minRequired = this.currentPrice.add(this.stepPrice);
        return bidAmount.compareTo(minRequired) >= 0;
    }

    public void acceptBid(BigDecimal bidAmount) {
        if (this.status != AuctionStatus.ACTIVE) {
            throw new AuctionNotActiveException();
        }
        if (LocalDateTime.now().isBefore(this.startTime) || LocalDateTime.now().isAfter(this.endTime)) {
            throw new AuctionEndedException();
        }
        if (bidAmount == null || bidAmount.compareTo(this.currentPrice.add(this.stepPrice)) < 0) {
            throw new BidTooLowException();
        }
        this.currentPrice = bidAmount;
        if (this.autoExtend != null && this.autoExtend && this.extendMinutes != null && this.extendMinutes > 0) {
            // Auto extend if bid is placed near end time (e.g., within 5 minutes)
            LocalDateTime nearEnd = this.endTime.minusMinutes(5);
            if (LocalDateTime.now().isAfter(nearEnd)) {
                this.endTime = this.endTime.plusMinutes(this.extendMinutes);
                this.status = AuctionStatus.EXTENDED;
            }
        }
    }

    public void close(boolean hasWinner) {
        if (hasWinner) {
            this.status = AuctionStatus.CLOSED;
        } else {
            this.status = AuctionStatus.FAILED;
        }
    }
}
