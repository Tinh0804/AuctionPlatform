package com.ecommerce.auctionplatform.auction.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class AuctionNotFoundException extends DomainException {
    public AuctionNotFoundException() {
        super(DomainErrorCode.AUCTION_NOT_FOUND);
    }
}
