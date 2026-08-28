package com.ecommerce.auctionplatform.payment.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.payment.domain.model.Transaction;
import com.ecommerce.auctionplatform.payment.domain.repository.TransactionRepository;
import com.ecommerce.auctionplatform.payment.infrastructure.persistence.mapper.PaymentPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {
    private final TransactionJpaRepository jpaRepository;
    private final PaymentPersistenceMapper mapper;

    @Override
    public Transaction save(Transaction transaction) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(transaction)));
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Transaction> findByWalletIdOrderByCreatedAtDesc(UUID walletId) {
        return jpaRepository.findByWalletIdOrderByCreatedAtDesc(walletId).stream().map(mapper::toDomain).toList();
    }

}
