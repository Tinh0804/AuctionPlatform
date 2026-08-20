package com.ecommerce.auctionplatform.auction.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.auction.domain.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BidRepository extends JpaRepository<Bid, UUID> {
    List<Bid> findByAuctionIdOrderByBidTimeDesc(UUID auctionId);
    List<Bid> findByAuctionIdOrderByBidAmountDesc(UUID auctionId);
    int countByAuctionId(UUID auctionId);
}
