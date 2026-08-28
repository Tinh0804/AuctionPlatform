package com.ecommerce.auctionplatform.identity.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.identity.infrastructure.persistence.entity.ReputationHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
interface ReputationHistoryJpaRepository extends JpaRepository<ReputationHistoryEntity, UUID> {
    List<ReputationHistoryEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
