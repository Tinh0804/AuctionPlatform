package com.ecommerce.auctionplatform.repository;

import com.ecommerce.auctionplatform.entity.User;
import com.ecommerce.auctionplatform.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    Optional<Wallet> findByUser(User user);
    Optional<Wallet> findByUserId(UUID userId);
}
