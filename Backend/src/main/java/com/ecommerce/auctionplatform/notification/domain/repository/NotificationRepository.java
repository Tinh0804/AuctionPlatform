package com.ecommerce.auctionplatform.notification.domain.repository;

import com.ecommerce.auctionplatform.notification.domain.model.Notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.ecommerce.auctionplatform.notification.domain.valueobject.NotificationSearchCriteria;
import com.ecommerce.auctionplatform.shared.domain.model.PageResult;

public interface NotificationRepository {
    Notification save(Notification notification);
    Optional<Notification> findById(UUID id);
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId);
    PageResult<Notification> search(NotificationSearchCriteria criteria);
    long countUnreadByUserId(UUID userId);
    void markAllAsReadByUserId(UUID userId);
    void deleteById(UUID id);
}
