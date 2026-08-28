package com.ecommerce.auctionplatform.notification.application.service;

import com.ecommerce.auctionplatform.notification.application.dto.command.AdminSendNotificationCommand;
import com.ecommerce.auctionplatform.notification.application.dto.response.NotificationResponse;
import com.ecommerce.auctionplatform.notification.application.port.in.AdminNotificationUseCase;
import com.ecommerce.auctionplatform.notification.application.port.in.NotificationUseCase;
import com.ecommerce.auctionplatform.notification.application.port.out.NotificationAudiencePort;
import com.ecommerce.auctionplatform.notification.domain.model.Notification;
import com.ecommerce.auctionplatform.notification.domain.repository.NotificationRepository;
import com.ecommerce.auctionplatform.notification.domain.valueobject.NotificationSearchCriteria;
import com.ecommerce.auctionplatform.shared.application.exception.AppException;
import com.ecommerce.auctionplatform.shared.application.exception.ErrorCode;
import com.ecommerce.auctionplatform.shared.application.model.PageQuery;
import com.ecommerce.auctionplatform.shared.application.port.out.CurrentUserProvider;
import com.ecommerce.auctionplatform.shared.domain.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminNotificationService implements AdminNotificationUseCase {
    private final NotificationRepository notificationRepository;
    private final NotificationUseCase notificationUseCase;
    private final NotificationAudiencePort audiencePort;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public PageResult<NotificationResponse> getAllNotifications(
            String type,
            Boolean read,
            PageQuery pageQuery
    ) {
        PageResult<Notification> page = notificationRepository.search(new NotificationSearchCriteria(
                type, read, pageQuery.pageNumber(), pageQuery.pageSize(), pageQuery.ascending()));
        Map<UUID, NotificationAudiencePort.AudienceUser> users = audiencePort.findByIds(
                page.items().stream().map(Notification::getUserId).collect(java.util.stream.Collectors.toSet()));
        return new PageResult<>(
                page.items().stream().map(notification -> toResponse(notification, users.get(notification.getUserId()))).toList(),
                page.pageNumber(), page.pageSize(), page.totalElements());
    }

    @Override
    public List<NotificationResponse> getAdminNotifications() {
        return notificationUseCase.getMyNotifications();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return notificationRepository.countUnreadByUserId(currentUserId());
    }

    @Override
    public void sendNotification(AdminSendNotificationCommand command) {
        String type = command.type() == null || command.type().isBlank()
                ? "ADMIN_ANNOUNCEMENT"
                : command.type();
        List<NotificationAudiencePort.AudienceUser> targets;
        if (command.userId() != null) {
            targets = List.of(audiencePort.findById(command.userId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
        } else if (command.roleName() != null && !command.roleName().isBlank()) {
            targets = audiencePort.findByRoleName(command.roleName().toUpperCase(Locale.ROOT));
        } else {
            targets = audiencePort.findAll();
        }
        targets.forEach(user -> notificationUseCase.sendNotification(
                user.id(), type, command.title(), command.content(), null, null));
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId) {
        Notification notification = notification(notificationId);
        notification.markAsRead();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        notificationRepository.markAllAsReadByUserId(currentUserId());
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId) {
        notification(notificationId);
        notificationRepository.deleteById(notificationId);
    }

    private Notification notification(UUID id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));
    }

    private UUID currentUserId() {
        return currentUserProvider.currentProfileId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTACATED));
    }

    private NotificationResponse toResponse(
            Notification notification,
            NotificationAudiencePort.AudienceUser user
    ) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getContent())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .recipientId(notification.getUserId())
                .recipientName(user == null ? null : user.name())
                .build();
    }
}
