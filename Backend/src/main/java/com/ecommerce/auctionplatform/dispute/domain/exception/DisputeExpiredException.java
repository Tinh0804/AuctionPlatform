package com.ecommerce.auctionplatform.dispute.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class DisputeExpiredException extends DomainException {
    public DisputeExpiredException() {
        super(DomainErrorCode.DISPUTE_EXPIRED);
    }
}
