package com.ecommerce.auctionplatform.payment.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class WalletPinWrongException extends DomainException {
    public WalletPinWrongException() {
        super(DomainErrorCode.WALLET_PIN_WRONG);
    }
}
