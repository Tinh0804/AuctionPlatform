package com.ecommerce.auctionplatform.shared.presentation.advice;

import com.ecommerce.auctionplatform.shared.application.exception.ErrorCode;
import org.springframework.http.HttpStatus;

final class ErrorHttpStatusMapper {
    private ErrorHttpStatusMapper() {
    }

    static HttpStatus toHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case CANNOT_BID_OWN_AUCTION, ALREADY_LEADING -> HttpStatus.BAD_REQUEST;
            case UNCATEGORIZED_EXCEPTION -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> {
                HttpStatus mapped = HttpStatus.resolve(errorCode.getStatus());
                yield mapped == null ? HttpStatus.INTERNAL_SERVER_ERROR : mapped;
            }
        };
    }
}
