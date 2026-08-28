package com.ecommerce.auctionplatform.identity.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class UnverifiedUserException extends DomainException {
    public UnverifiedUserException() {
        super(DomainErrorCode.UNVERIFIED_USER);
    }
}
