package com.ecommerce.auctionplatform.auction.infrastructure.persistence.repository;

import java.util.List;
import com.ecommerce.auctionplatform.auction.infrastructure.persistence.entity.AuctionEntity;
import java.util.Optional;
import com.ecommerce.auctionplatform.auction.domain.enums.AuctionStatus;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.UUID;
import com.ecommerce.auctionplatform.auction.domain.model.Auction;
import java.util.stream.Collectors;

import com.ecommerce.auctionplatform.auction.domain.repository.AuctionRepository;
import com.ecommerce.auctionplatform.auction.domain.valueobject.AuctionSearchCriteria;
import com.ecommerce.auctionplatform.shared.domain.model.PageResult;
import com.ecommerce.auctionplatform.auction.infrastructure.persistence.mapper.AuctionPersistenceMapper;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AuctionRepositoryImpl implements AuctionRepository {
    private final AuctionJpaRepository jpaRepository;
    private final AuctionPersistenceMapper mapper;

    @Override
    public Auction save(Auction auction) {
        AuctionEntity entity = mapper.toEntity(auction);
        AuctionEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Auction> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Auction> findByIdWithLock(UUID id) {
        return jpaRepository.findByIdWithLock(id).map(mapper::toDomain);
    }

    @Override
    public List<Auction> findByStatusInAndStartTimeBefore(List<AuctionStatus> statuses, LocalDateTime time) {
        return jpaRepository.findByStatusInAndStartTimeBefore(statuses, time)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Auction> findByStatusInAndEndTimeBefore(List<AuctionStatus> statuses, LocalDateTime time) {
        return jpaRepository.findByStatusInAndEndTimeBefore(statuses, time)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public PageResult<Auction> search(AuctionSearchCriteria criteria) {
        Sort.Direction direction = criteria.ascending() ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortBy = switch (criteria.sortBy()) {
            case "id", "startTime", "endTime", "currentPrice", "status", "createdAt" -> criteria.sortBy();
            default -> "createdAt";
        };
        PageRequest pageable = PageRequest.of(
                criteria.pageNumber(),
                criteria.pageSize(),
                Sort.by(direction, sortBy)
        );
        Page<AuctionEntity> page;
        if (criteria.status() == null && criteria.categoryId() == null) {
            page = jpaRepository.findAll(pageable);
        } else if (criteria.status() != null && criteria.categoryId() == null) {
            page = jpaRepository.searchByStatus(criteria.status(), pageable);
        } else if (criteria.status() == null) {
            page = jpaRepository.searchByCategory(criteria.categoryId(), pageable);
        } else {
            page = jpaRepository.searchByStatusAndCategory(criteria.status(), criteria.categoryId(), pageable);
        }
        return new PageResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

}
