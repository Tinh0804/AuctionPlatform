package com.ecommerce.auctionplatform.payment.domain.repository;

import com.ecommerce.auctionplatform.payment.domain.model.Wallet;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {
    Wallet save(Wallet wallet);
    Optional<Wallet> findById(UUID id);
    Optional<Wallet> findByUserId(UUID userId);
    Optional<Wallet> findByIdForUpdate(UUID id);
    Optional<Wallet> findByUserIdForUpdate(UUID userId);
}
