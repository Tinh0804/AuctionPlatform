package com.ecommerce.auctionplatform.identity.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.identity.domain.enums.ProviderType;
import com.ecommerce.auctionplatform.identity.infrastructure.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
interface AccountJpaRepository extends JpaRepository<AccountEntity, UUID> {
     boolean existsByUsername(String userName);

     Optional<AccountEntity> findByUsername(String userName);

     Optional<AccountEntity> findByProviderAndProviderId(ProviderType provider, String providerId);
}
