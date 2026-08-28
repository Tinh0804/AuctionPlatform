package com.ecommerce.auctionplatform.auction.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class AuctionNotActiveException extends DomainException {
    public AuctionNotActiveException() {
        super(DomainErrorCode.BAD_REQUEST);
    }
}
