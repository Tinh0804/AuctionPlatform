package com.ecommerce.auctionplatform.auction.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class CannotBidOwnAuctionException extends DomainException {
    public CannotBidOwnAuctionException() {
        super(DomainErrorCode.CANNOT_BID_OWN_AUCTION);
    }
}
