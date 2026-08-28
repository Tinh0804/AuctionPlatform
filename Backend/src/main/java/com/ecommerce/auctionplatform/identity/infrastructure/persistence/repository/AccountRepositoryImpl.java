package com.ecommerce.auctionplatform.identity.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.identity.domain.enums.ProviderType;
import com.ecommerce.auctionplatform.identity.domain.model.Account;
import com.ecommerce.auctionplatform.identity.domain.repository.AccountRepository;
import com.ecommerce.auctionplatform.identity.infrastructure.persistence.mapper.UserPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepository {
    private final AccountJpaRepository jpaRepository;
    private final UserPersistenceMapper mapper;

    @Override
    public Account save(Account account) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(account)));
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByProviderAndProviderId(ProviderType provider, String providerId) {
        return jpaRepository.findByProviderAndProviderId(provider, providerId).map(mapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }
}
