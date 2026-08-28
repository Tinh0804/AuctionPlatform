package com.ecommerce.auctionplatform.identity.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class LowReputationException extends DomainException {
    public LowReputationException() {
        super(DomainErrorCode.LOW_REPUTATION);
    }
}
