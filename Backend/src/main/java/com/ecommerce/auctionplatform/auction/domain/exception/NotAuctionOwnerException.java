package com.ecommerce.auctionplatform.auction.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class NotAuctionOwnerException extends DomainException {
    public NotAuctionOwnerException() {
        super(DomainErrorCode.NOT_AUCTON_OWNER);
    }
}
