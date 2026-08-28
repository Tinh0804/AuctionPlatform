package com.ecommerce.auctionplatform.identity.domain.repository;

import com.ecommerce.auctionplatform.identity.domain.enums.ProviderType;
import com.ecommerce.auctionplatform.identity.domain.model.Account;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(UUID id);
    Optional<Account> findByUsername(String username);
    Optional<Account> findByProviderAndProviderId(ProviderType provider, String providerId);
    boolean existsByUsername(String username);
}
