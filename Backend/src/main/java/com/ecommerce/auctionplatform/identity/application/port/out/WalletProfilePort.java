package com.ecommerce.auctionplatform.identity.application.port.out;

import com.ecommerce.auctionplatform.identity.application.dto.response.WalletResponse;

import java.util.UUID;

public interface WalletProfilePort {
    WalletResponse getWallet(UUID userId);
}
