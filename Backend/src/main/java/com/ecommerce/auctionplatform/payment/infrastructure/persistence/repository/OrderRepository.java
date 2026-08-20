package com.ecommerce.auctionplatform.payment.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.payment.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByBuyerIdOrderByCreatedAtDesc(UUID buyerId);
    List<Order> findBySellerIdOrderByCreatedAtDesc(UUID sellerId);
}
