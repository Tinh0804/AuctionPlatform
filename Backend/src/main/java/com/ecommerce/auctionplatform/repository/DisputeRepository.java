package com.ecommerce.auctionplatform.repository;

import com.ecommerce.auctionplatform.entity.Dispute;
import com.ecommerce.auctionplatform.entity.enums.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, UUID> {
    List<Dispute> findByClaimantIdOrderByCreatedAtDesc(UUID claimantId);
    List<Dispute> findByOrderIdOrderByCreatedAtDesc(UUID orderId);
    List<Dispute> findAllByOrderByCreatedAtDesc();
    Optional<Dispute> findByOrderIdAndStatus(UUID orderId, DisputeStatus status);
    boolean existsByOrderIdAndStatusIn(UUID orderId, List<DisputeStatus> statuses);
}
