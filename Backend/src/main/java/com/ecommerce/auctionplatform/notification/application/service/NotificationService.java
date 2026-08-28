package com.ecommerce.auctionplatform.notification.application.service;

import com.ecommerce.auctionplatform.notification.application.dto.response.NotificationResponse;
import com.ecommerce.auctionplatform.notification.domain.model.Notification;
import com.ecommerce.auctionplatform.shared.application.exception.AppException;
import com.ecommerce.auctionplatform.shared.application.exception.ErrorCode;
import com.ecommerce.auctionplatform.notification.domain.repository.NotificationRepository;
import com.ecommerce.auctionplatform.notification.application.port.out.NotificationSenderPort;
import com.ecommerce.auctionplatform.shared.application.port.out.CurrentUserProvider;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import com.ecommerce.auctionplatform.notification.application.port.in.NotificationUseCase;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NotificationService implements NotificationUseCase {

    NotificationRepository notificationRepository;
    NotificationSenderPort notificationSender;
    CurrentUserProvider currentUserProvider;

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

        if (!notification.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        notification.markAsRead();
        notification = notificationRepository.save(notification);
    }


    @Transactional
    public void sendNotification(UUID userId, String type, String title, String content,
                                  String referenceType, UUID referenceId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .content(content)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .build();
        notificationRepository.save(notification);


        NotificationResponse response = toResponse(notification);
        notificationSender.send(userId, response);
    }
    private UUID getCurrentUserId(){
       return currentUserProvider.currentProfileId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTACATED));
        
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
