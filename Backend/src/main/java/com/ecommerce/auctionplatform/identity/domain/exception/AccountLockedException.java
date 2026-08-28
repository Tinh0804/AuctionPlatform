package com.ecommerce.auctionplatform.identity.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class AccountLockedException extends DomainException {
    public AccountLockedException() {
        super(DomainErrorCode.ACCOUNT_LOCKED);
    }
}
