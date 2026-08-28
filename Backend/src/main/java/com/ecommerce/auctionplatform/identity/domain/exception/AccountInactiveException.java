package com.ecommerce.auctionplatform.identity.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class AccountInactiveException extends DomainException {
    public AccountInactiveException() {
        super(DomainErrorCode.ACCOUNT_INACTIVE);
    }
}
