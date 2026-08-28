package com.ecommerce.auctionplatform.auction.infrastructure.persistence.repository;

import java.util.List;
import com.ecommerce.auctionplatform.auction.infrastructure.persistence.entity.BidEntity;
import java.util.Optional;
import org.springframework.stereotype.Component;
import java.util.UUID;
import com.ecommerce.auctionplatform.auction.domain.model.Bid;
import java.util.stream.Collectors;

import com.ecommerce.auctionplatform.auction.domain.repository.BidRepository;
import com.ecommerce.auctionplatform.auction.infrastructure.persistence.mapper.AuctionPersistenceMapper;
import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class BidRepositoryImpl implements BidRepository {
    private final BidJpaRepository jpaRepository;
    private final AuctionPersistenceMapper mapper;

    @Override
    public Bid save(Bid bid) {
        BidEntity entity = mapper.toEntity(bid);
        BidEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<Bid> findByAuctionIdOrderByBidAmountDesc(UUID auctionId) {
        return jpaRepository.findByAuctionIdOrderByBidAmountDesc(auctionId)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Bid> findTopByAuctionIdOrderByBidAmountDesc(UUID auctionId) {
        return jpaRepository.findTopByAuctionIdOrderByBidAmountDesc(auctionId).map(mapper::toDomain);
    }

    @Override
    public int countByAuctionId(UUID auctionId) {
        return jpaRepository.countByAuctionId(auctionId);
    }
    @Override
    public List<Bid> findByAuctionIdOrderByBidTimeDesc(UUID auctionId) {
        return jpaRepository.findByAuctionIdOrderByBidTimeDesc(auctionId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

}