package com.ecommerce.auctionplatform.auction.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import com.ecommerce.auctionplatform.auction.domain.enums.AuctionRecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ecommerce.auctionplatform.auction.infrastructure.persistence.entity.AuctionRecordEntity;
import java.util.UUID;
import java.util.stream.Collectors;



@Repository
interface AuctionRecordJpaRepository extends JpaRepository<AuctionRecordEntity, UUID> {
    Optional<AuctionRecordEntity> findByAuctionIdAndUserId(UUID auctionId, UUID userId);
    List<AuctionRecordEntity> findByAuctionIdAndStatusOrderByWinningRankAsc(UUID auctionId, AuctionRecordStatus status);
}
