package com.ecommerce.auctionplatform.service;

import com.ecommerce.auctionplatform.dto.request.AdminSendNotificationRequest;
import com.ecommerce.auctionplatform.dto.respose.NotificationResponse;
import com.ecommerce.auctionplatform.entity.Notification;
import com.ecommerce.auctionplatform.entity.User;
import com.ecommerce.auctionplatform.entity.enums.PredefinedRole;
import com.ecommerce.auctionplatform.exception.AppException;
import com.ecommerce.auctionplatform.exception.ErrorCode;
import com.ecommerce.auctionplatform.repository.NotificationRepository;
import com.ecommerce.auctionplatform.repository.UserRepository;
import com.ecommerce.auctionplatform.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NotificationService {

    NotificationRepository notificationRepository;
    UserRepository userRepository;
    SimpMessagingTemplate messagingTemplate;

    // ════════════════════════════════════════════
    // ── User-facing methods (existing) ──
    // ════════════════════════════════════════════

    public List<NotificationResponse> getMyNotifications() {
        UUID userId = getCurrentUserId();
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream().map(this::toResponse).toList();
    }

    @Transactional
    public void markAsRead(UUID notificationId) {
        UUID userId = getCurrentUserId();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Transactional
    public void sendNotification(User user, String type, String title, String content,
                                  String referenceType, UUID referenceId) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .content(content)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .build();
        notificationRepository.save(notification);

        NotificationResponse response = toResponse(notification);
        try {
            messagingTemplate.convertAndSend(
                    "/topic/notification/" + user.getId(), response);
        } catch (Exception e) {
            log.warn("Failed to push WebSocket notification to user {}: {}", user.getId(), e.getMessage());
        }
    }

    // ════════════════════════════════════════════
    // ── Admin methods (new) ──
    // ════════════════════════════════════════════

    /**
     * Lấy tất cả notification trong hệ thống (phân trang + filter)
     */
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public Page<NotificationResponse> getAllNotifications(String type, Boolean isRead, Pageable pageable) {
        Page<Notification> page;

        if (type != null && isRead != null) {
            page = notificationRepository.findByTypeAndIsReadOrderByCreatedAtDesc(type, isRead, pageable);
        } else if (type != null) {
            page = notificationRepository.findByTypeOrderByCreatedAtDesc(type, pageable);
        } else if (isRead != null) {
            page = notificationRepository.findByIsReadOrderByCreatedAtDesc(isRead, pageable);
        } else {
            page = notificationRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return page.map(this::toResponseWithUser);
    }

    /**
     * Lấy notification riêng của admin (cho badge chuông)
     */
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public List<NotificationResponse> getAdminNotifications() {
        UUID adminUserId = getCurrentUserId();
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(adminUserId);
        return notifications.stream().map(this::toResponse).toList();
    }

    /**
     * Đếm số notification chưa đọc của admin
     */
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public long getUnreadCountForAdmin() {
        UUID adminUserId = getCurrentUserId();
        return notificationRepository.countByUserIdAndIsReadFalse(adminUserId);
    }

    /**
     * Admin gửi thông báo: cho 1 user, cho vai trò, hoặc broadcast tất cả
     */
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    @Transactional
    public void adminSendNotification(AdminSendNotificationRequest request) {
        String notifType = (request.getType() != null && !request.getType().isBlank())
                ? request.getType() : "ADMIN_ANNOUNCEMENT";

        if (request.getUserId() != null) {
            // Gửi cho 1 user cụ thể
            User targetUser = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            sendNotification(targetUser, notifType, request.getTitle(), request.getContent(), null, null);
            log.info("Admin sent notification to user {}: {}", request.getUserId(), request.getTitle());
        } else if (request.getRoleName() != null && !request.getRoleName().isBlank()) {
            // Gửi cho 1 vai trò cụ thể
            List<User> usersInRole = userRepository.findByAccount_Role_Name(request.getRoleName().toUpperCase());
            for (User user : usersInRole) {
                sendNotification(user, notifType, request.getTitle(), request.getContent(), null, null);
            }
            log.info("Admin broadcast notification to role {}: {} users, title: {}", request.getRoleName(), usersInRole.size(), request.getTitle());
        } else {
            // Broadcast cho tất cả user
            List<User> allUsers = userRepository.findAll();
            for (User user : allUsers) {
                sendNotification(user, notifType, request.getTitle(), request.getContent(), null, null);
            }
            log.info("Admin broadcast notification to {} users: {}", allUsers.size(), request.getTitle());
        }
    }

    /**
     * Admin đánh dấu đã đọc 1 notification (không kiểm tra ownership)
     */
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    @Transactional
    public void adminMarkAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    /**
     * Admin đánh dấu tất cả notification của mình đã đọc
     */
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    @Transactional
    public void markAllAsReadForAdmin() {
        UUID adminUserId = getCurrentUserId();
        notificationRepository.markAllAsReadByUserId(adminUserId);
    }

    /**
     * Admin xóa 1 notification
     */
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    @Transactional
    public void deleteNotification(UUID notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new AppException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }
        notificationRepository.deleteById(notificationId);
    }

    // ════════════════════════════════════════════
    // ── Private helpers ──
    // ════════════════════════════════════════════

    private UUID getCurrentUserId(){
       return UUID.fromString(
                SecurityUtils.getCurrentProfileId()
                        .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTACATED)));
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getContent())
                .referenceType(n.getReferenceType())
                .referenceId(n.getReferenceId())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }

    /**
     * Bao gồm thêm tên user nhận (cho bảng admin)
     */
    private NotificationResponse toResponseWithUser(Notification n) {
        NotificationResponse response = toResponse(n);
        if (n.getUser() != null) {
            response.setRecipientName(n.getUser().getName());
            response.setRecipientId(n.getUser().getId());
        }
        return response;
    }
}

