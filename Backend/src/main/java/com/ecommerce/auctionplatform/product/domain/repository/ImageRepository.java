package com.ecommerce.auctionplatform.product.domain.repository;

import com.ecommerce.auctionplatform.product.domain.model.Image;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImageRepository {
    Image save(Image image);
    Optional<Image> findById(UUID id);
    List<Image> findByProductId(UUID productId);
    List<Image> findByProductIdOrderByIsCoverDesc(UUID productId);
    Optional<Image> findFirstByProductIdOrderByIsCoverDesc(UUID productId);
    void deleteByProductId(UUID productId);

}
