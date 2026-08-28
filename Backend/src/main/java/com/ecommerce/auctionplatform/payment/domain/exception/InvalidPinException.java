package com.ecommerce.auctionplatform.payment.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class InvalidPinException extends DomainException {
    public InvalidPinException() {
        super(DomainErrorCode.INVALID_PIN);
    }
}
