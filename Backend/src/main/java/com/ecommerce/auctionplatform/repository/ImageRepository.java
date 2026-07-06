package com.ecommerce.auctionplatform.repository;

import com.ecommerce.auctionplatform.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ImageRepository extends JpaRepository<Image, UUID> {
    List<Image> findByProductId(UUID productId);
    List<Image> findByProductIdOrderByIsCoverDesc(UUID productId);
}
