package com.ecommerce.auctionplatform.payment.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class OrderNotEligibleForReviewException extends DomainException {
    public OrderNotEligibleForReviewException() {
        super(DomainErrorCode.ORDER_NOT_ELIGIBLE_FOR_REVIEW);
    }
}
