package com.ecommerce.auctionplatform.payment.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class WalletFrozenException extends DomainException {
    public WalletFrozenException() {
        super(DomainErrorCode.WALLET_FROZEN);
    }
}
