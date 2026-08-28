package com.ecommerce.auctionplatform.shared.application.model;

public record PageQuery(int pageNumber, int pageSize, String sortBy, boolean ascending) {
    public PageQuery {
        if (pageNumber < 0 || pageSize < 1) {
            throw new IllegalArgumentException("Invalid page query");
        }
    }
}
