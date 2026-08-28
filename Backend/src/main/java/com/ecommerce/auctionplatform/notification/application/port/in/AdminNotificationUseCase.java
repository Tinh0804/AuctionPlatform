package com.ecommerce.auctionplatform.notification.application.port.in;

import com.ecommerce.auctionplatform.notification.application.dto.command.AdminSendNotificationCommand;
import com.ecommerce.auctionplatform.notification.application.dto.response.NotificationResponse;
import com.ecommerce.auctionplatform.shared.application.model.PageQuery;
import com.ecommerce.auctionplatform.shared.domain.model.PageResult;

import java.util.List;
import java.util.UUID;

public interface AdminNotificationUseCase {
    PageResult<NotificationResponse> getAllNotifications(String type, Boolean read, PageQuery pageQuery);

    List<NotificationResponse> getAdminNotifications();

    long getUnreadCount();

    void sendNotification(AdminSendNotificationCommand command);

    void markAsRead(UUID notificationId);

    void markAllAsRead();

    void deleteNotification(UUID notificationId);
}
