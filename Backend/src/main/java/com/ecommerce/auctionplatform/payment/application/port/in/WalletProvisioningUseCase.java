package com.ecommerce.auctionplatform.payment.application.port.in;

import com.ecommerce.auctionplatform.payment.application.dto.response.WalletSnapshot;

import java.util.UUID;

/**
 * Explicitly represents the lazy wallet-provisioning behavior used by account profiles.
 */
public interface WalletProvisioningUseCase {
    WalletSnapshot getOrCreate(UUID userId);
}
