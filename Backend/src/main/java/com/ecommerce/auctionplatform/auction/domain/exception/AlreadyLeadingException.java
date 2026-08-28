package com.ecommerce.auctionplatform.auction.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class AlreadyLeadingException extends DomainException {
    public AlreadyLeadingException() {
        super(DomainErrorCode.ALREADY_LEADING);
    }
}
