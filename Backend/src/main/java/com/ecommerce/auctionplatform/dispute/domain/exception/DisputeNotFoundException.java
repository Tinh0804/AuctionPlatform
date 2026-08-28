package com.ecommerce.auctionplatform.dispute.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class DisputeNotFoundException extends DomainException {
    public DisputeNotFoundException() {
        super(DomainErrorCode.DISPUTE_NOT_FOUND);
    }
}
