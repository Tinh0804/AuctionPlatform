package com.ecommerce.auctionplatform.payment.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class WalletPinNotSetException extends DomainException {
    public WalletPinNotSetException() {
        super(DomainErrorCode.WALLET_PIN_NOT_SET);
    }
}
