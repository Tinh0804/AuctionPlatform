package com.ecommerce.auctionplatform.dispute.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class DisputeAlreadyResolvedException extends DomainException {
    public DisputeAlreadyResolvedException() {
        super(DomainErrorCode.DISPUTE_ALREADY_RESOLVED);
    }
}
