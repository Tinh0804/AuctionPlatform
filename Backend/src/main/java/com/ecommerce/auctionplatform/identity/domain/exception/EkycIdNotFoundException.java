package com.ecommerce.auctionplatform.identity.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class EkycIdNotFoundException extends DomainException {
    public EkycIdNotFoundException() {
        super(DomainErrorCode.EKYC_ID_NOT_FOUND);
    }
}
