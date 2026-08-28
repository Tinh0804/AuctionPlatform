package com.ecommerce.auctionplatform.notification.domain.valueobject;

public record NotificationSearchCriteria(
        String type,
        Boolean read,
        int pageNumber,
        int pageSize,
        boolean ascending
) {
    public NotificationSearchCriteria {
        if (pageNumber < 0 || pageSize < 1) {
            throw new IllegalArgumentException("Invalid notification search page");
        }
    }
}
