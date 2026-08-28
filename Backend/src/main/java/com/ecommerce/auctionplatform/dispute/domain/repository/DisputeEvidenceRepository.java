package com.ecommerce.auctionplatform.dispute.domain.repository;

import com.ecommerce.auctionplatform.dispute.domain.model.DisputeEvidence;

import java.util.List;
import java.util.UUID;

public interface DisputeEvidenceRepository {
    DisputeEvidence save(DisputeEvidence evidence);

    List<DisputeEvidence> findByDisputeIdOrderBySortOrder(UUID disputeId);
}
