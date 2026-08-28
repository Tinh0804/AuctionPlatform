package com.ecommerce.auctionplatform.product.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.product.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {
}
