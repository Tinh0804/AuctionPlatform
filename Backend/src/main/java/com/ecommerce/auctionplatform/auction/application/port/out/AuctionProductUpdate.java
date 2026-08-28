package com.ecommerce.auctionplatform.auction.application.port.out;

import java.util.UUID;

public record AuctionProductUpdate(
        String name,
        String origin,
        String condition,
        String manufactureYear,
        UUID categoryId
) {
}
