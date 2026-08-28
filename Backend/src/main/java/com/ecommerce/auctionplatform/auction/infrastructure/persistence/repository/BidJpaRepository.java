package com.ecommerce.auctionplatform.auction.infrastructure.persistence.repository;

import java.util.List;
import com.ecommerce.auctionplatform.auction.infrastructure.persistence.entity.BidEntity;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.stream.Collectors;



@Repository
interface BidJpaRepository extends JpaRepository<BidEntity, UUID> {
    int countByAuctionId(UUID auctionId);
    List<BidEntity> findByAuctionIdOrderByBidTimeDesc(UUID auctionId);
    List<BidEntity> findByAuctionIdOrderByBidAmountDesc(UUID auctionId);
    Optional<BidEntity> findTopByAuctionIdOrderByBidAmountDesc(UUID auctionId);
}
