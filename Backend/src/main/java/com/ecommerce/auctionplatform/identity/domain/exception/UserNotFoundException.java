package com.ecommerce.auctionplatform.identity.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class UserNotFoundException extends DomainException {
    public UserNotFoundException() {
        super(DomainErrorCode.USER_NOT_FOUND);
    }
}
