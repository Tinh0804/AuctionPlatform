package com.ecommerce.auctionplatform.notification.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.notification.infrastructure.persistence.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
interface NotificationJpaRepository extends JpaRepository<NotificationEntity, UUID>, JpaSpecificationExecutor<NotificationEntity> {
    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<NotificationEntity> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId);
    long countByUserIdAndIsReadFalse(UUID userId);

    @Modifying
    @Query("update NotificationEntity n set n.isRead = true, n.readAt = CURRENT_TIMESTAMP "
            + "where n.userId = :userId and n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") UUID userId);
}
