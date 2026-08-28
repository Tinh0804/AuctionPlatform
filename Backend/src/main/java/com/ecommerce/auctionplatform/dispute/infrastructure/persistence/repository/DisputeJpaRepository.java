package com.ecommerce.auctionplatform.dispute.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.dispute.infrastructure.persistence.entity.DisputeEntity;
import com.ecommerce.auctionplatform.dispute.domain.enums.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface DisputeJpaRepository extends JpaRepository<DisputeEntity, UUID> {
    List<DisputeEntity> findByClaimantIdOrderByCreatedAtDesc(UUID claimantId);
    List<DisputeEntity> findByOrderIdOrderByCreatedAtDesc(UUID orderId);
    List<DisputeEntity> findAllByOrderByCreatedAtDesc();
    Optional<DisputeEntity> findByOrderIdAndStatus(UUID orderId, DisputeStatus status);
    boolean existsByOrderIdAndStatusIn(UUID orderId, List<DisputeStatus> statuses);
}
