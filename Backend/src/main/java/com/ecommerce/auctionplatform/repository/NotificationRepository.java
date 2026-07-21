package com.ecommerce.auctionplatform.repository;

import com.ecommerce.auctionplatform.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    // ── User queries (existing) ──
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId);

    // ── Admin queries (new) ──
    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Notification> findByTypeOrderByCreatedAtDesc(String type, Pageable pageable);
    Page<Notification> findByIsReadOrderByCreatedAtDesc(Boolean isRead, Pageable pageable);
    Page<Notification> findByTypeAndIsReadOrderByCreatedAtDesc(String type, Boolean isRead, Pageable pageable);

    long countByUserIdAndIsReadFalse(UUID userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") UUID userId);
}

