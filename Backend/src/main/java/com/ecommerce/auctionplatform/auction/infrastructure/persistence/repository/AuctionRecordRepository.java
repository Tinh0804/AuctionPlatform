package com.ecommerce.auctionplatform.auction.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.auction.domain.model.AuctionRecord;
import com.ecommerce.auctionplatform.auction.domain.enums.AuctionRecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuctionRecordRepository extends JpaRepository<AuctionRecord, UUID> {
    List<AuctionRecord> findByAuctionIdAndStatusOrderByWinningRankAsc(UUID auctionId, AuctionRecordStatus status);
    Optional<AuctionRecord> findByAuctionIdAndWinningRank(UUID auctionId, int rank);
    List<AuctionRecord> findByStatusAndExpiryTimeBefore(AuctionRecordStatus status, LocalDateTime now);
}
