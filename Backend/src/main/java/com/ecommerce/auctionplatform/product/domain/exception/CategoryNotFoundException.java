package com.ecommerce.auctionplatform.product.domain.exception;

import com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;

public class CategoryNotFoundException extends DomainException {
    public CategoryNotFoundException() {
        super(DomainErrorCode.CATEGORY_NOT_FOUND);
    }
}
