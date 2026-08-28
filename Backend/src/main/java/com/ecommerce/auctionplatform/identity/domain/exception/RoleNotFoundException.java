package com.ecommerce.auctionplatform.identity.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class RoleNotFoundException extends DomainException {
    public RoleNotFoundException() {
        super(DomainErrorCode.ROLE_NOT_FOUND);
    }
}
