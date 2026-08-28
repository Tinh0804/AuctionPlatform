package com.ecommerce.auctionplatform.payment.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class InvalidAmountException extends DomainException {
    public InvalidAmountException() {
        super(DomainErrorCode.BAD_REQUEST);
    }
}
