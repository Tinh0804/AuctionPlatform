package com.ecommerce.auctionplatform.identity.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class RoleNotExistsException extends DomainException {
    public RoleNotExistsException() {
        super(DomainErrorCode.ROLE_NOT_EXISTS);
    }
}
