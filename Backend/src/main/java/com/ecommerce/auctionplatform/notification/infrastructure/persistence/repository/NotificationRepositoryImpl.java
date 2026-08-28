package com.ecommerce.auctionplatform.notification.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.notification.domain.model.Notification;
import com.ecommerce.auctionplatform.notification.domain.repository.NotificationRepository;
import com.ecommerce.auctionplatform.notification.infrastructure.persistence.mapper.NotificationPersistenceMapper;
import lombok.RequiredArgsConstructor;
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
}
