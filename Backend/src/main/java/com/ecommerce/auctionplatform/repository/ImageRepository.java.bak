package com.ecommerce.auctionplatform.repository;

import com.ecommerce.auctionplatform.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ecommerce.auctionplatform.entity.enums.ImageReferenceType;

@Repository
public interface ImageRepository extends JpaRepository<Image, UUID> {


    List<Image> findByReferenceTypeAndReferenceIdOrderBySortOrderAscCreatedAtAsc(
            ImageReferenceType referenceType, UUID referenceId);

    List<Image> findByReferenceTypeAndReferenceIdOrderByIsCoverDescSortOrderAsc(
            ImageReferenceType referenceType, UUID referenceId);
    Optional<Image> findFirstByReferenceTypeAndReferenceIdOrderByIsCoverDesc(ImageReferenceType referenceType, UUID referenceId);

    default Optional<Image> findFirstByProductIdOrderByIsCoverDesc(UUID productId) {
        return findFirstByReferenceTypeAndReferenceIdOrderByIsCoverDesc(ImageReferenceType.PRODUCT, productId);
    }


    void deleteByReferenceTypeAndReferenceId(ImageReferenceType referenceType, UUID referenceId);

    // Product

    default List<Image> findByProductId(UUID productId) {
        return findByReferenceTypeAndReferenceIdOrderBySortOrderAscCreatedAtAsc(ImageReferenceType.PRODUCT, productId);
    }

    default List<Image> findByProductIdOrderByIsCoverDesc(UUID productId) {
        return findByReferenceTypeAndReferenceIdOrderByIsCoverDescSortOrderAsc(ImageReferenceType.PRODUCT, productId);
    }

    // Dispute

    default List<Image> findByDisputeId(UUID disputeId) {
        return findByReferenceTypeAndReferenceIdOrderBySortOrderAscCreatedAtAsc(ImageReferenceType.DISPUTE, disputeId);
    }
}
