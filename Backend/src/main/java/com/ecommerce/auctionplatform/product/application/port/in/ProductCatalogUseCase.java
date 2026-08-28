package com.ecommerce.auctionplatform.product.application.port.in;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Public application contract for consumers that need product catalogue data.
 * Consumers never receive product domain objects or repositories.
 */
public interface ProductCatalogUseCase {
    Optional<UUID> createProduct(ProductDraft draft);

    void addProductImage(UUID productId, String fileUrl, boolean cover, int sortOrder);

    Optional<ProductSummary> findProduct(UUID productId);

    List<MediaSummary> findProductImages(UUID productId);

    record ProductDraft(
            UUID sellerId,
            UUID categoryId,
            String name,
            String condition,
            String description,
            String origin
    ) {
    }

    record ProductSummary(UUID id, String name, String categoryName) {
    }

    record MediaSummary(String fileUrl, boolean cover) {
    }
}
