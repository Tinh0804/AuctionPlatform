package com.ecommerce.auctionplatform.shared.presentation.advice;

import lombok.Data;

@Data
public class AppException extends RuntimeException {
    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
