package com.ecommerce.auctionplatform.product.application.service;

import com.ecommerce.auctionplatform.product.application.port.in.ProductCatalogUseCase;
import com.ecommerce.auctionplatform.product.domain.enums.ProductCondition;
import com.ecommerce.auctionplatform.product.domain.enums.ProductStatus;
import com.ecommerce.auctionplatform.product.domain.model.Image;
import com.ecommerce.auctionplatform.product.domain.model.Product;
import com.ecommerce.auctionplatform.product.domain.repository.CategoryRepository;
import com.ecommerce.auctionplatform.product.domain.repository.ImageRepository;
import com.ecommerce.auctionplatform.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductCatalogService implements ProductCatalogUseCase {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;

    @Override
    @Transactional
    public Optional<UUID> createProduct(ProductDraft draft) {
        return categoryRepository.findById(draft.categoryId()).map(category -> productRepository.save(Product.builder()
                .userId(draft.sellerId())
                .category(category)
                .name(draft.name())
                .condition(parseCondition(draft.condition()))
                .description(draft.description())
                .origin(draft.origin())
                .status(ProductStatus.PENDING)
                .build()).getId());
    }

    @Override
    @Transactional
    public void addProductImage(UUID productId, String fileUrl, boolean cover, int sortOrder) {
        imageRepository.save(Image.builder()
                .productId(productId)
                .fileUrl(fileUrl)
                .isCover(cover)
                .sortOrder(sortOrder)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductSummary> findProduct(UUID productId) {
        return productRepository.findById(productId).map(product -> new ProductSummary(
                product.getId(),
                product.getName(),
                product.getCategory() == null ? null : product.getCategory().getName()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaSummary> findProductImages(UUID productId) {
        return imageRepository.findByProductId(productId).stream()
                .map(this::toSummary)
                .toList();
    }

    private MediaSummary toSummary(Image image) {
        return new MediaSummary(image.getFileUrl(), Boolean.TRUE.equals(image.getIsCover()));
    }

    private ProductCondition parseCondition(String condition) {
        String normalized = "USED".equalsIgnoreCase(condition) ? "LIKE_NEW" : condition;
        return ProductCondition.valueOf(normalized.toUpperCase(Locale.ROOT));
    }
}
