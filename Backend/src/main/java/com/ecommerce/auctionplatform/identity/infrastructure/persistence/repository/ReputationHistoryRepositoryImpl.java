package com.ecommerce.auctionplatform.identity.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.identity.domain.model.ReputationHistory;
import com.ecommerce.auctionplatform.identity.domain.repository.ReputationHistoryRepository;
import com.ecommerce.auctionplatform.identity.infrastructure.persistence.mapper.ReputationPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReputationHistoryRepositoryImpl implements ReputationHistoryRepository {
    private final ReputationHistoryJpaRepository jpaRepository;
    private final ReputationPersistenceMapper mapper;

    @Override
    public ReputationHistory save(ReputationHistory history) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(history)));
    }

    @Override
    public List<ReputationHistory> findByUserIdOrderByCreatedAtDesc(UUID userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(mapper::toDomain).toList();
    }
}
