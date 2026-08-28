package com.ecommerce.auctionplatform.payment.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class OrderNotFoundException extends DomainException {
    public OrderNotFoundException() {
        super(DomainErrorCode.ORDER_NOT_FOUND);
    }
}
