package com.ecommerce.auctionplatform.repository;

import com.ecommerce.auctionplatform.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {
    List<Order> findByBuyerIdOrderByCreatedAtDesc(UUID buyerId);
    List<Order> findBySellerIdOrderByCreatedAtDesc(UUID sellerId);
}
