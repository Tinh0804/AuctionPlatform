package com.ecommerce.auctionplatform.payment.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class PaymentMethodNotSupportedException extends DomainException {
    public PaymentMethodNotSupportedException() {
        super(DomainErrorCode.PAYMENT_METHOD_NOT_SUPPORTED);
    }
}
