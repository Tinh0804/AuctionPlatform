package com.ecommerce.auctionplatform.product.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.product.infrastructure.persistence.entity.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ImageJpaRepository extends JpaRepository<ImageEntity, UUID> {
    List<ImageEntity> findByProductIdOrderBySortOrderAscCreatedAtAsc(UUID productId);

    List<ImageEntity> findByProductIdOrderByIsCoverDescSortOrderAsc(UUID productId);

    Optional<ImageEntity> findFirstByProductIdOrderByIsCoverDesc(UUID productId);

    void deleteByProductId(UUID productId);
}
