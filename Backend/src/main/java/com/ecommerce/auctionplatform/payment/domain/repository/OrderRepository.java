package com.ecommerce.auctionplatform.payment.domain.repository;

import com.ecommerce.auctionplatform.payment.domain.model.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID id);
    List<Order> findByBuyerIdOrderByCreatedAtDesc(UUID buyerId);
    List<Order> findBySellerIdOrderByCreatedAtDesc(UUID sellerId);
}
