package com.ecommerce.auctionplatform.dispute.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.dispute.domain.model.DisputeEvidence;
import com.ecommerce.auctionplatform.dispute.domain.repository.DisputeEvidenceRepository;
import com.ecommerce.auctionplatform.dispute.infrastructure.persistence.entity.DisputeEvidenceEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DisputeEvidenceRepositoryImpl implements DisputeEvidenceRepository {
    private final DisputeEvidenceJpaRepository jpaRepository;

    @Override
    public DisputeEvidence save(DisputeEvidence evidence) {
        return toDomain(jpaRepository.save(toEntity(evidence)));
    }

    @Override
    public List<DisputeEvidence> findByDisputeIdOrderBySortOrder(UUID disputeId) {
        return jpaRepository.findByDisputeIdOrderBySortOrderAsc(disputeId).stream()
                .map(this::toDomain)
                .toList();
    }

    private DisputeEvidenceEntity toEntity(DisputeEvidence evidence) {
        return DisputeEvidenceEntity.builder()
                .id(evidence.getId())
                .disputeId(evidence.getDisputeId())
                .fileUrl(evidence.getFileUrl())
                .sortOrder(evidence.getSortOrder())
                .description(evidence.getDescription())
                .createdAt(evidence.getCreatedAt())
                .build();
    }

    private DisputeEvidence toDomain(DisputeEvidenceEntity entity) {
        return DisputeEvidence.builder()
                .id(entity.getId())
                .disputeId(entity.getDisputeId())
                .fileUrl(entity.getFileUrl())
                .sortOrder(entity.getSortOrder())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
