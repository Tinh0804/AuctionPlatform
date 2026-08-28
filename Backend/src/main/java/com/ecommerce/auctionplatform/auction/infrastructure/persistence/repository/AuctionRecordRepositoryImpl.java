package com.ecommerce.auctionplatform.auction.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import com.ecommerce.auctionplatform.auction.domain.enums.AuctionRecordStatus;
import java.util.UUID;
import com.ecommerce.auctionplatform.auction.domain.model.AuctionRecord;
import com.ecommerce.auctionplatform.auction.infrastructure.persistence.entity.AuctionRecordEntity;
import java.util.stream.Collectors;

import com.ecommerce.auctionplatform.auction.domain.repository.AuctionRecordRepository;
import com.ecommerce.auctionplatform.auction.infrastructure.persistence.mapper.AuctionPersistenceMapper;
import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class AuctionRecordRepositoryImpl implements AuctionRecordRepository {
    private final AuctionRecordJpaRepository jpaRepository;
    private final AuctionPersistenceMapper mapper;

    @Override
    public AuctionRecord save(AuctionRecord record) {
        AuctionRecordEntity entity = mapper.toEntity(record);
        AuctionRecordEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<AuctionRecord> findByAuctionIdAndUserId(UUID auctionId, UUID userId) {
        return jpaRepository.findByAuctionIdAndUserId(auctionId, userId).map(mapper::toDomain);
    }

    @Override
    public Optional<AuctionRecord> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<AuctionRecord> findByAuctionIdAndStatusOrderByWinningRankAsc(UUID auctionId, AuctionRecordStatus status) {
        return jpaRepository.findByAuctionIdAndStatusOrderByWinningRankAsc(auctionId, status)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}
