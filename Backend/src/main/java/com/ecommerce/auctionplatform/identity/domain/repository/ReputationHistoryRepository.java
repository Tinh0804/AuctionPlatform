package com.ecommerce.auctionplatform.identity.domain.repository;

import com.ecommerce.auctionplatform.identity.domain.model.ReputationHistory;

import java.util.List;
import java.util.UUID;

public interface ReputationHistoryRepository {
    ReputationHistory save(ReputationHistory history);
    List<ReputationHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
