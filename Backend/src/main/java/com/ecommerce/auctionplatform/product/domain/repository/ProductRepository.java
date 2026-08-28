package com.ecommerce.auctionplatform.product.domain.repository;

import com.ecommerce.auctionplatform.product.domain.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(UUID id);
    List<Product> findAll();
    long countByCategoryId(UUID categoryId);
}
