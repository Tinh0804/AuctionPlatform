package com.ecommerce.auctionplatform.identity.domain.valueobject;

public record UserSearchCriteria(
        String keyword,
        int pageNumber,
        int pageSize,
        String sortBy,
        boolean ascending
) {
    public UserSearchCriteria {
        if (pageNumber < 0 || pageSize < 1) {
            throw new IllegalArgumentException("Invalid user search page");
        }
    }
}
