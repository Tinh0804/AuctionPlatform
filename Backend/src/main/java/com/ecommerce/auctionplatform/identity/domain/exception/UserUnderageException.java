package com.ecommerce.auctionplatform.identity.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class UserUnderageException extends DomainException {
    public UserUnderageException() {
        super(DomainErrorCode.USER_UNDERAGE);
    }
}
