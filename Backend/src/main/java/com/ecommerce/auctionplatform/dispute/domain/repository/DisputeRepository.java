package com.ecommerce.auctionplatform.dispute.domain.repository;

import com.ecommerce.auctionplatform.dispute.domain.enums.DisputeStatus;
import com.ecommerce.auctionplatform.dispute.domain.model.Dispute;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DisputeRepository {
    Dispute save(Dispute dispute);
    Optional<Dispute> findById(UUID id);
    List<Dispute> findByClaimantIdOrderByCreatedAtDesc(UUID claimantId);
    List<Dispute> findByOrderIdOrderByCreatedAtDesc(UUID orderId);
    List<Dispute> findAllByOrderByCreatedAtDesc();
    Optional<Dispute> findByOrderIdAndStatus(UUID orderId, DisputeStatus status);
    boolean existsByOrderIdAndStatusIn(UUID orderId, List<DisputeStatus> statuses);
    long count();
}
