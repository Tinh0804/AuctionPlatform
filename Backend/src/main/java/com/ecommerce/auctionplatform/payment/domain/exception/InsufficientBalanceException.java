package com.ecommerce.auctionplatform.payment.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class InsufficientBalanceException extends DomainException {
    public InsufficientBalanceException() {
        super(DomainErrorCode.INSUFFICIENT_BALANCE);
    }
}
