package com.ecommerce.auctionplatform.auction.application.port.out;

import java.util.UUID;

public record ProductDraft(
        UUID sellerId,
        UUID categoryId,
        String name,
        String condition,
        String description,
        String origin
) {
}
