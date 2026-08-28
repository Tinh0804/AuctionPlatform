package com.ecommerce.auctionplatform.payment.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.payment.domain.model.Order;
import com.ecommerce.auctionplatform.payment.domain.repository.OrderRepository;
import com.ecommerce.auctionplatform.payment.infrastructure.persistence.mapper.PaymentPersistenceMapper;
import com.ecommerce.auctionplatform.payment.domain.valueobject.OrderSearchCriteria;
import com.ecommerce.auctionplatform.shared.domain.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

    @Override
    public PageResult<Order> search(OrderSearchCriteria criteria) {
        Specification<com.ecommerce.auctionplatform.payment.infrastructure.persistence.entity.OrderEntity> spec =
                criteria.status() == null
                        ? Specification.where(null)
                        : (root, query, cb) -> cb.equal(root.get("status"), criteria.status());
        String sortProperty = switch (criteria.sortBy()) {
            case "status", "totalAmount", "updatedAt" -> criteria.sortBy();
            default -> "createdAt";
        };
        Sort sort = Sort.by(criteria.ascending() ? Sort.Direction.ASC : Sort.Direction.DESC, sortProperty);
        var page = jpaRepository.findAll(spec, PageRequest.of(criteria.pageNumber(), criteria.pageSize(), sort));
        return new PageResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements());
    }

}
