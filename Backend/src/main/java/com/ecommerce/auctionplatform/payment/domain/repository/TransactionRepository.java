package com.ecommerce.auctionplatform.payment.domain.repository;

import com.ecommerce.auctionplatform.payment.domain.model.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(UUID id);
    List<Transaction> findByWalletIdOrderByCreatedAtDesc(UUID walletId);
}
