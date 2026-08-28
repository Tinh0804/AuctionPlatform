package com.ecommerce.auctionplatform.dispute.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.dispute.infrastructure.persistence.entity.DisputeEvidenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface DisputeEvidenceJpaRepository extends JpaRepository<DisputeEvidenceEntity, UUID> {
    List<DisputeEvidenceEntity> findByDisputeIdOrderBySortOrderAsc(UUID disputeId);
}
