package com.ecommerce.auctionplatform.payment.domain.valueobject;

import com.ecommerce.auctionplatform.payment.domain.enums.OrderStatus;

public record OrderSearchCriteria(
        OrderStatus status,
        int pageNumber,
        int pageSize,
        String sortBy,
        boolean ascending
) {
    public OrderSearchCriteria {
        if (pageNumber < 0 || pageSize < 1) {
            throw new IllegalArgumentException("Invalid order search page");
        }
    }
}
