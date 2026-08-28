package com.ecommerce.auctionplatform.auction.domain.repository;

import java.util.List;
import com.ecommerce.auctionplatform.auction.domain.model.Bid;
import java.util.UUID;
import java.util.Optional;
import java.util.stream.Collectors;


public interface BidRepository {
    Bid save(Bid bid);
    List<Bid> findByAuctionIdOrderByBidAmountDesc(UUID auctionId);
    Optional<Bid> findTopByAuctionIdOrderByBidAmountDesc(UUID auctionId);

    int countByAuctionId(UUID auctionId);
    List<Bid> findByAuctionIdOrderByBidTimeDesc(UUID auctionId);

}