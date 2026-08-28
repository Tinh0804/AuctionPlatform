package com.ecommerce.auctionplatform.dispute.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.dispute.domain.enums.DisputeStatus;
import com.ecommerce.auctionplatform.dispute.domain.model.Dispute;
import com.ecommerce.auctionplatform.dispute.domain.repository.DisputeRepository;
import com.ecommerce.auctionplatform.dispute.infrastructure.persistence.mapper.DisputePersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DisputeRepositoryImpl implements DisputeRepository {
    private final DisputeJpaRepository jpaRepository;
    private final DisputePersistenceMapper mapper;

    @Override
    public Dispute save(Dispute dispute) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(dispute)));
    }

    @Override
    public Optional<Dispute> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Dispute> findByClaimantIdOrderByCreatedAtDesc(UUID claimantId) {
        return jpaRepository.findByClaimantIdOrderByCreatedAtDesc(claimantId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Dispute> findByOrderIdOrderByCreatedAtDesc(UUID orderId) {
        return jpaRepository.findByOrderIdOrderByCreatedAtDesc(orderId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Dispute> findAllByOrderByCreatedAtDesc() {
        return jpaRepository.findAllByOrderByCreatedAtDesc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Dispute> findByOrderIdAndStatus(UUID orderId, DisputeStatus status) {
        return jpaRepository.findByOrderIdAndStatus(orderId, status).map(mapper::toDomain);
    }

    @Override
    public boolean existsByOrderIdAndStatusIn(UUID orderId, List<DisputeStatus> statuses) {
        return jpaRepository.existsByOrderIdAndStatusIn(orderId, statuses);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }
}
