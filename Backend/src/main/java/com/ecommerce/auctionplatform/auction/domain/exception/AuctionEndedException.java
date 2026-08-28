package com.ecommerce.auctionplatform.auction.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class AuctionEndedException extends DomainException {
    public AuctionEndedException() {
        super(DomainErrorCode.AUCTION_ENDED);
    }
}
