package com.ecommerce.auctionplatform.shared.domain.model;

import java.util.List;

public record PageResult<T>(
        List<T> items,
        int pageNumber,
        int pageSize,
        long totalElements
) {
    public PageResult {
        items = List.copyOf(items);
    }
}
