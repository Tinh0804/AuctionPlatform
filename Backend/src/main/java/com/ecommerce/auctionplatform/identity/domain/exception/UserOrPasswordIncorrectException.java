package com.ecommerce.auctionplatform.identity.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class UserOrPasswordIncorrectException extends DomainException {
    public UserOrPasswordIncorrectException() {
        super(DomainErrorCode.USER_OR_PASSWORD_INCORRECT);
    }
}
