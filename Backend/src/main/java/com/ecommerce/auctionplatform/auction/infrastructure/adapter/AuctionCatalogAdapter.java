package com.ecommerce.auctionplatform.auction.infrastructure.adapter;

import com.ecommerce.auctionplatform.auction.application.port.out.AuctionCatalogPort;
import com.ecommerce.auctionplatform.auction.application.port.out.AuctionImageView;
import com.ecommerce.auctionplatform.auction.application.port.out.AuctionProductView;
import com.ecommerce.auctionplatform.auction.application.port.out.ProductDraft;
import com.ecommerce.auctionplatform.product.application.port.in.ProductCatalogUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuctionCatalogAdapter implements AuctionCatalogPort {
    private final ProductCatalogUseCase productCatalogUseCase;

    @Override
    public Optional<UUID> createProduct(ProductDraft draft) {
        return productCatalogUseCase.createProduct(new ProductCatalogUseCase.ProductDraft(
                draft.sellerId(),
                draft.categoryId(),
                draft.name(),
                draft.condition(),
                draft.description(),
                draft.origin()));
    }

    @Override
    public void addImage(UUID productId, String fileUrl, boolean cover, int sortOrder) {
        productCatalogUseCase.addProductImage(productId, fileUrl, cover, sortOrder);
    }

    @Override
    public Optional<AuctionProductView> findProduct(UUID productId) {
        return productCatalogUseCase.findProduct(productId).map(product -> new AuctionProductView(
                product.id(), product.name(), product.categoryName()));
    }

    @Override
    public List<AuctionImageView> findImages(UUID productId) {
        return productCatalogUseCase.findProductImages(productId).stream()
                .map(image -> new AuctionImageView(image.fileUrl(), image.cover()))
                .toList();
    }
}
