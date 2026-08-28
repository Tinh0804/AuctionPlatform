package com.ecommerce.auctionplatform.identity.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class InvalidEkycImageException extends DomainException {
    public InvalidEkycImageException() {
        super(DomainErrorCode.INVALID_EKYC_IMAGE);
    }
}
