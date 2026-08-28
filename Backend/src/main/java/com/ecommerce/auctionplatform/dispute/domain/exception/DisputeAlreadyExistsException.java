package com.ecommerce.auctionplatform.dispute.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class DisputeAlreadyExistsException extends DomainException {
    public DisputeAlreadyExistsException() {
        super(DomainErrorCode.DISPUTE_ALREADY_EXISTS);
    }
}
