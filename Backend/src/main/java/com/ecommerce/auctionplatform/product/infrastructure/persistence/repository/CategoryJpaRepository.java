package com.ecommerce.auctionplatform.product.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.product.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
interface CategoryJpaRepository extends JpaRepository<CategoryEntity, UUID> {
}
