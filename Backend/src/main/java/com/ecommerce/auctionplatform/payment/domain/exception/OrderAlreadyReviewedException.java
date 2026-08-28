package com.ecommerce.auctionplatform.payment.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class OrderAlreadyReviewedException extends DomainException {
    public OrderAlreadyReviewedException() {
        super(DomainErrorCode.ORDER_ALREADY_REVIEWED);
    }
}
