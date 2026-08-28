package com.ecommerce.auctionplatform.payment.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.payment.domain.model.Order;
import com.ecommerce.auctionplatform.payment.domain.repository.OrderRepository;
import com.ecommerce.auctionplatform.payment.infrastructure.persistence.mapper.PaymentPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderJpaRepository jpaRepository;
    private final PaymentPersistenceMapper mapper;

    @Override
    public Order save(Order order) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(order)));
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Order> findByBuyerIdOrderByCreatedAtDesc(UUID buyerId) {
        return jpaRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Order> findBySellerIdOrderByCreatedAtDesc(UUID sellerId) {
        return jpaRepository.findBySellerIdOrderByCreatedAtDesc(sellerId).stream().map(mapper::toDomain).toList();
    }

}
