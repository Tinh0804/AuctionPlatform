package com.ecommerce.auctionplatform.payment.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class TransactionNotFoundException extends DomainException {
    public TransactionNotFoundException() {
        super(DomainErrorCode.TRANSACTION_NOT_FOUND);
    }
}
