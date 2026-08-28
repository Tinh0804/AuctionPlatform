package com.ecommerce.auctionplatform.payment.application.service;

import com.ecommerce.auctionplatform.payment.application.dto.response.WalletSnapshot;
import com.ecommerce.auctionplatform.payment.application.port.in.WalletProvisioningUseCase;
import com.ecommerce.auctionplatform.payment.domain.enums.WalletStatus;
import com.ecommerce.auctionplatform.payment.domain.model.Wallet;
import com.ecommerce.auctionplatform.payment.domain.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletProvisioningService implements WalletProvisioningUseCase {
    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public WalletSnapshot getOrCreate(UUID userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> walletRepository.save(Wallet.builder()
                        .userId(userId)
                        .availableBalance(BigDecimal.ZERO)
                        .frozenBalance(BigDecimal.ZERO)
                        .status(WalletStatus.ACTIVE)
                        .build()));

        return new WalletSnapshot(
                wallet.getId(),
                wallet.getAvailableBalance(),
                wallet.getFrozenBalance(),
                wallet.getPinCode() != null && !wallet.getPinCode().isBlank(),
                wallet.getStatus()
        );
    }
}
