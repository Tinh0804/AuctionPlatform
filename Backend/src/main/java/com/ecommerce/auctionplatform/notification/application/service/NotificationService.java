package com.ecommerce.auctionplatform.notification.application.service;

import com.ecommerce.auctionplatform.notification.application.dto.response.NotificationResponse;
import com.ecommerce.auctionplatform.notification.domain.model.Notification;
import com.ecommerce.auctionplatform.user.domain.model.User;
import com.ecommerce.auctionplatform.shared.presentation.advice.AppException;
import com.ecommerce.auctionplatform.shared.presentation.advice.ErrorCode;
import com.ecommerce.auctionplatform.notification.infrastructure.persistence.repository.NotificationRepository;
import com.ecommerce.auctionplatform.user.infrastructure.persistence.repository.UserRepository;
import com.ecommerce.auctionplatform.shared.infrastructure.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
}
