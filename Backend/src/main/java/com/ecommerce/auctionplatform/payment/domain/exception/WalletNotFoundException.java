package com.ecommerce.auctionplatform.payment.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class WalletNotFoundException extends DomainException {
    public WalletNotFoundException() {
        super(DomainErrorCode.WALLET_NOT_FOUND);
    }
}
