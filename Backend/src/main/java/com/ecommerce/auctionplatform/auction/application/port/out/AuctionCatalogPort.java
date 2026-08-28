package com.ecommerce.auctionplatform.auction.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuctionCatalogPort {
    Optional<UUID> createProduct(ProductDraft draft);

    void addImage(UUID productId, String fileUrl, boolean cover, int sortOrder);

    Optional<AuctionProductView> findProduct(UUID productId);

    List<AuctionImageView> findImages(UUID productId);
}
