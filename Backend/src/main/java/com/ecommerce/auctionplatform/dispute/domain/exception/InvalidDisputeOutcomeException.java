package com.ecommerce.auctionplatform.dispute.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class InvalidDisputeOutcomeException extends DomainException {
    public InvalidDisputeOutcomeException() {
        super(DomainErrorCode.INVALID_DISPUTE_OUTCOME);
    }
}
