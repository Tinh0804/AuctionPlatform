package com.ecommerce.auctionplatform.product.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.product.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
}
