package com.ecommerce.auctionplatform.payment.domain.model;

import com.ecommerce.auctionplatform.payment.domain.exception.InsufficientBalanceException;
import com.ecommerce.auctionplatform.payment.domain.exception.InvalidAmountException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WalletTest {
    @Test
    void releasingFrozenMoneyMovesItExactlyOnceToAvailableBalance() {
        Wallet wallet = Wallet.builder()
                .availableBalance(BigDecimal.ZERO)
                .frozenBalance(new BigDecimal("100000"))
                .build();

        wallet.unfreezeBalance(new BigDecimal("100000"));

        assertEquals(0, wallet.getAvailableBalance().compareTo(new BigDecimal("100000")));
        assertEquals(0, wallet.getFrozenBalance().compareTo(BigDecimal.ZERO));
    }

    @Test
    void walletRejectsInvalidOrUnavailableAmounts() {
        Wallet wallet = Wallet.builder()
                .availableBalance(new BigDecimal("50000"))
                .frozenBalance(BigDecimal.ZERO)
                .build();

        assertThrows(InvalidAmountException.class, () -> wallet.addBalance(BigDecimal.ZERO));
        assertThrows(InsufficientBalanceException.class,
                () -> wallet.deductBalance(new BigDecimal("50001")));
    }
}
