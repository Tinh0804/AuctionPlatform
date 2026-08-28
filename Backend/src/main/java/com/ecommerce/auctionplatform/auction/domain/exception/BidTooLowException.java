package com.ecommerce.auctionplatform.auction.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class BidTooLowException extends DomainException {
    public BidTooLowException() {
        super(DomainErrorCode.BID_TOO_LOW);
    }
}
