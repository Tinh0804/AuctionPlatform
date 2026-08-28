package com.ecommerce.auctionplatform.payment.domain.model;
import com.ecommerce.auctionplatform.payment.domain.enums.WalletStatus;
import com.ecommerce.auctionplatform.payment.domain.exception.InsufficientBalanceException;
import com.ecommerce.auctionplatform.payment.domain.exception.InvalidAmountException;
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
public class Wallet {
    UUID id;
    UUID userId;

    @Builder.Default
    BigDecimal availableBalance = BigDecimal.ZERO;

    @Builder.Default
    BigDecimal frozenBalance = BigDecimal.ZERO;

    String pinCode;

    @Builder.Default
    WalletStatus status = WalletStatus.ACTIVE;

    String notes;

    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    LocalDateTime updatedAt;

    public void setupPin(String encodedPin) {
        this.pinCode = encodedPin;
        this.updatedAt = LocalDateTime.now();
    }

    public void freezeBalance(BigDecimal amount) {
        requirePositive(amount);
        if (this.availableBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }
        this.availableBalance = this.availableBalance.subtract(amount);
        this.frozenBalance = this.frozenBalance.add(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public void unfreezeBalance(BigDecimal amount) {
        requirePositive(amount);
        if (this.frozenBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }
        this.frozenBalance = this.frozenBalance.subtract(amount);
        this.availableBalance = this.availableBalance.add(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public void addBalance(BigDecimal amount) {
        requirePositive(amount);
        this.availableBalance = this.availableBalance.add(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public void deductBalance(BigDecimal amount) {
        requirePositive(amount);
        if (this.availableBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }
        this.availableBalance = this.availableBalance.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public void deductFrozenBalance(BigDecimal amount) {
        requirePositive(amount);
        if (this.frozenBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }
        this.frozenBalance = this.frozenBalance.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public void addFrozenBalance(BigDecimal amount) {
        requirePositive(amount);
        this.frozenBalance = this.frozenBalance.add(amount);
        this.updatedAt = LocalDateTime.now();
    }

    private void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidAmountException();
        }
    }
}
