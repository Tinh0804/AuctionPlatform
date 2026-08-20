package com.ecommerce.auctionplatform.payment.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.user.domain.model.User;
import com.ecommerce.auctionplatform.payment.domain.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    Optional<Wallet> findByUser(User user);
    Optional<Wallet> findByUserId(UUID userId);
}
