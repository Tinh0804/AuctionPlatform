package com.ecommerce.auctionplatform.notification.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class NotificationNotFoundException extends DomainException {
    public NotificationNotFoundException() {
        super(DomainErrorCode.NOTIFICATION_NOT_FOUND);
    }
}
