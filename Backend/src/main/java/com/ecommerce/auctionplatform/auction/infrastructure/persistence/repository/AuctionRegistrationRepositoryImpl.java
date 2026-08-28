package com.ecommerce.auctionplatform.auction.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.auction.domain.model.AuctionRegistration;
import com.ecommerce.auctionplatform.auction.domain.repository.AuctionRegistrationRepository;
import com.ecommerce.auctionplatform.auction.infrastructure.persistence.entity.AuctionRegistrationEntity;
import com.ecommerce.auctionplatform.auction.infrastructure.persistence.mapper.AuctionPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuctionRegistrationRepositoryImpl implements AuctionRegistrationRepository {
    private final AuctionRegistrationJpaRepository jpaRepository;
    private final AuctionPersistenceMapper mapper;

    @Override
    public AuctionRegistration save(AuctionRegistration registration) {
        AuctionRegistrationEntity entity = mapper.toEntity(registration);
        AuctionRegistrationEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<AuctionRegistration> findByAuctionIdAndUserId(UUID auctionId, UUID userId) {
        return jpaRepository.findByAuctionIdAndUserId(auctionId, userId).map(mapper::toDomain);
    }

    @Override
    public List<AuctionRegistration> findByAuctionId(UUID auctionId) {
        return jpaRepository.findByAuctionId(auctionId)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<AuctionRegistration> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
