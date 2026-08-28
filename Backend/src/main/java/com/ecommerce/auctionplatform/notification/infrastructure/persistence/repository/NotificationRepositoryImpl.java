package com.ecommerce.auctionplatform.notification.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.notification.domain.model.Notification;
import com.ecommerce.auctionplatform.notification.domain.repository.NotificationRepository;
import com.ecommerce.auctionplatform.notification.infrastructure.persistence.mapper.NotificationPersistenceMapper;
import com.ecommerce.auctionplatform.notification.domain.valueobject.NotificationSearchCriteria;
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
public class NotificationRepositoryImpl implements NotificationRepository {
    private final NotificationJpaRepository jpaRepository;
    private final NotificationPersistenceMapper mapper;

    @Override
    public Notification save(Notification notification) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(notification)));
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId) {
        return jpaRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public PageResult<Notification> search(NotificationSearchCriteria criteria) {
        Specification<com.ecommerce.auctionplatform.notification.infrastructure.persistence.entity.NotificationEntity> spec =
                (root, query, cb) -> {
                    var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
                    if (criteria.type() != null && !criteria.type().isBlank()) {
                        predicates.add(cb.equal(root.get("type"), criteria.type()));
                    }
                    if (criteria.read() != null) {
                        predicates.add(cb.equal(root.get("isRead"), criteria.read()));
                    }
                    return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
                };
        Sort sort = Sort.by(criteria.ascending() ? Sort.Direction.ASC : Sort.Direction.DESC, "createdAt");
        var page = jpaRepository.findAll(spec, PageRequest.of(criteria.pageNumber(), criteria.pageSize(), sort));
        return new PageResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Override
    public long countUnreadByUserId(UUID userId) {
        return jpaRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public void markAllAsReadByUserId(UUID userId) {
        jpaRepository.markAllAsReadByUserId(userId);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
