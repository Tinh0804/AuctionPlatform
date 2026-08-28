package com.ecommerce.auctionplatform.identity.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class EmailExistedException extends DomainException {
    public EmailExistedException() {
        super(DomainErrorCode.EMAIL_EXISTED);
    }
}
