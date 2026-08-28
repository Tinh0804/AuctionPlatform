package com.ecommerce.auctionplatform.dispute.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class OrderNotEligibleForDisputeException extends DomainException {
    public OrderNotEligibleForDisputeException() {
        super(DomainErrorCode.ORDER_NOT_ELIGIBLE_FOR_DISPUTE);
    }
}
