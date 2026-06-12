package com.ecommerce.auctionplatform.repository;

import com.ecommerce.auctionplatform.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
     boolean existsByUsername(String userName);

     Optional<Account> findByUsername(String userName);

     Optional<Account> findByProviderAndProviderId(String provider, String providerId);
}
