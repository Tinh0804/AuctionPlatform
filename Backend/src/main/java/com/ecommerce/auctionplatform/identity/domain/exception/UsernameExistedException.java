package com.ecommerce.auctionplatform.identity.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class UsernameExistedException extends DomainException {
    public UsernameExistedException() {
        super(DomainErrorCode.USERNAME_EXISTED);
    }
}
