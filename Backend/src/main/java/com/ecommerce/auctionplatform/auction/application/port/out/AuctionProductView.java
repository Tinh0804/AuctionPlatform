package com.ecommerce.auctionplatform.auction.application.port.out;

import java.util.UUID;

public record AuctionProductView(
        UUID id,
        String name,
        String categoryName,
        String origin,
        String condition,
        String manufactureYear
) {
}
