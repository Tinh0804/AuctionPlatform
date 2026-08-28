package com.ecommerce.auctionplatform.auction.domain.valueobject;

import com.ecommerce.auctionplatform.auction.domain.enums.AuctionStatus;

import java.util.UUID;

public record AuctionSearchCriteria(
        AuctionStatus status,
        UUID categoryId,
        int pageNumber,
        int pageSize,
        String sortBy,
        boolean ascending
) {
    public AuctionSearchCriteria {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("pageNumber must be non-negative");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be positive");
        }
    }
}
