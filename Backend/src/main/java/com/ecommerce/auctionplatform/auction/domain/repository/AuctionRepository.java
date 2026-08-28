package com.ecommerce.auctionplatform.auction.domain.repository;

import java.util.List;
import java.util.Optional;
import com.ecommerce.auctionplatform.auction.domain.enums.AuctionStatus;
import com.ecommerce.auctionplatform.auction.domain.valueobject.AuctionSearchCriteria;
import com.ecommerce.auctionplatform.shared.domain.model.PageResult;
import java.util.UUID;
import com.ecommerce.auctionplatform.auction.domain.model.Auction;
import java.time.LocalDateTime;

public interface AuctionRepository {
    Auction save(Auction auction);
    Optional<Auction> findById(UUID id);
    Optional<Auction> findByIdWithLock(UUID id);
    List<Auction> findByStatusInAndStartTimeBefore(List<AuctionStatus> statuses, LocalDateTime time);
    List<Auction> findByStatusInAndEndTimeBefore(List<AuctionStatus> statuses, LocalDateTime time);
    long count();

    PageResult<Auction> search(AuctionSearchCriteria criteria);

}
