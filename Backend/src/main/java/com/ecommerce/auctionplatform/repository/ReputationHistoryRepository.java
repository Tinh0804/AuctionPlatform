package com.ecommerce.auctionplatform.repository;

import com.ecommerce.auctionplatform.entity.ReputationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReputationHistoryRepository extends JpaRepository<ReputationHistory, UUID> {
    List<ReputationHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
