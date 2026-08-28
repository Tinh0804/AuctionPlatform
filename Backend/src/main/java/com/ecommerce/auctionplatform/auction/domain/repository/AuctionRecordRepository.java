package com.ecommerce.auctionplatform.auction.domain.repository;

import java.util.List;
import java.util.Optional;
import com.ecommerce.auctionplatform.auction.domain.enums.AuctionRecordStatus;
import java.util.UUID;
import com.ecommerce.auctionplatform.auction.domain.model.AuctionRecord;
import java.util.stream.Collectors;


public interface AuctionRecordRepository {
    AuctionRecord save(AuctionRecord record);
    Optional<AuctionRecord> findByAuctionIdAndUserId(UUID auctionId, UUID userId);
    Optional<AuctionRecord> findById(UUID id);
    List<AuctionRecord> findByAuctionIdAndStatusOrderByWinningRankAsc(UUID auctionId, AuctionRecordStatus status);
}
