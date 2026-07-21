package com.ecommerce.auctionplatform.controller;

import com.ecommerce.auctionplatform.dto.request.AdminSendNotificationRequest;
import com.ecommerce.auctionplatform.dto.respose.APIResponse;
import com.ecommerce.auctionplatform.dto.respose.NotificationResponse;
import com.ecommerce.auctionplatform.service.NotificationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/admin/notifications")
public class AdminNotificationController {

    NotificationService notificationService;

    /**
     * Lấy tất cả notification trong hệ thống (phân trang + filter)
     */
    @GetMapping
    public APIResponse<Page<NotificationResponse>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isRead
    ) {
        Page<NotificationResponse> result = notificationService.getAllNotifications(
                type, isRead,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return APIResponse.<Page<NotificationResponse>>builder()
                .message("All notifications retrieved")
                .result(result)
                .build();
    }

    /**
     * Lấy thông báo riêng của admin (cho badge chuông)
     */
    @GetMapping("/my")
    public APIResponse<List<NotificationResponse>> getAdminNotifications() {
        return APIResponse.<List<NotificationResponse>>builder()
                .message("Admin notifications retrieved")
                .result(notificationService.getAdminNotifications())
                .build();
    }

    /**
     * Đếm số notification chưa đọc của admin
     */
    @GetMapping("/unread-count")
    public APIResponse<Long> getUnreadCount() {
        return APIResponse.<Long>builder()
                .message("Unread count retrieved")
                .result(notificationService.getUnreadCountForAdmin())
                .build();
    }

    /**
     * Admin gửi thông báo đến 1 user hoặc broadcast tất cả
     */
    @PostMapping("/send")
    public APIResponse<Void> sendNotification(@RequestBody @Valid AdminSendNotificationRequest request) {
        notificationService.adminSendNotification(request);
        return APIResponse.<Void>builder()
                .message("Notification sent successfully")
                .build();
    }

    /**
     * Đánh dấu 1 notification đã đọc
     */
    @PutMapping("/{id}/read")
    public APIResponse<Void> markAsRead(@PathVariable UUID id) {
        notificationService.adminMarkAsRead(id);
        return APIResponse.<Void>builder()
                .message("Notification marked as read")
                .build();
    }

    /**
     * Đánh dấu tất cả notification của admin đã đọc
     */
    @PutMapping("/read-all")
    public APIResponse<Void> markAllAsRead() {
        notificationService.markAllAsReadForAdmin();
        return APIResponse.<Void>builder()
                .message("All notifications marked as read")
                .build();
    }

    /**
     * Xóa 1 notification
     */
    @DeleteMapping("/{id}")
    public APIResponse<Void> deleteNotification(@PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return APIResponse.<Void>builder()
                .message("Notification deleted")
                .build();
    }
}
