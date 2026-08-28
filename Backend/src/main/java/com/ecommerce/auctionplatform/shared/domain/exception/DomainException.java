package com.ecommerce.auctionplatform.shared.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;

public class DomainException extends RuntimeException {
    private final DomainErrorCode errorCode;

    public DomainException(DomainErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
    }

    public DomainException(DomainErrorCode errorCode, String message) {
        super(message != null ? message : errorCode.name());
        this.errorCode = errorCode;
    }

    public DomainErrorCode getErrorCode() {
        return errorCode;
    }

    public String getCode() {
        return errorCode != null ? errorCode.name() : getMessage();
    }
}

