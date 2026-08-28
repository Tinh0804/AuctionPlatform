package com.ecommerce.auctionplatform.integration.adapter;

import com.ecommerce.auctionplatform.payment.application.dto.response.WalletSnapshot;
import com.ecommerce.auctionplatform.payment.application.port.in.WalletProvisioningUseCase;
import com.ecommerce.auctionplatform.identity.application.dto.response.WalletResponse;
import com.ecommerce.auctionplatform.identity.application.port.out.WalletProfilePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserWalletProfileAdapter implements WalletProfilePort {
    private final WalletProvisioningUseCase walletProvisioningUseCase;

    @Override
    public WalletResponse getWallet(UUID userId) {
        WalletSnapshot wallet = walletProvisioningUseCase.getOrCreate(userId);
        return WalletResponse.builder()
                .id(wallet.id())
                .availableBalance(wallet.availableBalance())
                .frozenBalance(wallet.frozenBalance())
                .hasPin(wallet.hasPin())
                .status(wallet.status().name())
                .build();
    }
}
