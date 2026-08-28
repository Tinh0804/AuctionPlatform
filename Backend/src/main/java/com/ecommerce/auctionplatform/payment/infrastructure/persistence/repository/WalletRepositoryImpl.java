package com.ecommerce.auctionplatform.payment.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.payment.domain.model.Wallet;
import com.ecommerce.auctionplatform.payment.domain.repository.WalletRepository;
import com.ecommerce.auctionplatform.payment.infrastructure.persistence.mapper.PaymentPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class WalletRepositoryImpl implements WalletRepository {
    private final WalletJpaRepository jpaRepository;
    private final PaymentPersistenceMapper mapper;

    @Override
    public Wallet save(Wallet wallet) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(wallet)));
    }

    @Override
    public Optional<Wallet> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Wallet> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public Optional<Wallet> findByIdForUpdate(UUID id) {
        return jpaRepository.findByIdForUpdate(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Wallet> findByUserIdForUpdate(UUID userId) {
        return jpaRepository.findByUserIdForUpdate(userId).map(mapper::toDomain);
    }
}
