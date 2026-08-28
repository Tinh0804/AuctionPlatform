package com.ecommerce.auctionplatform.shared.presentation.advice;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandleTest {
    private final GlobalExceptionHandle handler = new GlobalExceptionHandle();

    @Test
    void customBusinessCodeUsesAValidHttpStatus() {
        var response = handler.handleDomainException(new DomainException(DomainErrorCode.CANNOT_BID_OWN_AUCTION));

        assertEquals(400, response.getStatusCode().value());
        assertEquals(1014, response.getBody().getStatus());
    }
}
