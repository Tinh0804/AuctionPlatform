package com.ecommerce.auctionplatform.notification.presentation.rest;

import com.ecommerce.auctionplatform.notification.application.dto.command.AdminSendNotificationCommand;
import com.ecommerce.auctionplatform.notification.application.port.in.AdminNotificationUseCase;
import com.ecommerce.auctionplatform.notification.presentation.dto.request.AdminSendNotificationRequest;
import com.ecommerce.auctionplatform.notification.presentation.dto.response.NotificationResponse;
import com.ecommerce.auctionplatform.notification.presentation.mapper.NotificationResponseMapper;
import com.ecommerce.auctionplatform.shared.application.model.PageQuery;
import com.ecommerce.auctionplatform.shared.domain.model.PageResult;
import com.ecommerce.auctionplatform.shared.presentation.response.APIResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationController {
    private final AdminNotificationUseCase adminNotificationUseCase;
    private final NotificationResponseMapper responseMapper;

    @GetMapping
    public APIResponse<Page<NotificationResponse>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isRead
    ) {
        var result = adminNotificationUseCase.getAllNotifications(
                        type, isRead, new PageQuery(page, size, "createdAt", false));
        var pageable = PageRequest.of(result.pageNumber(), result.pageSize());
        Page<NotificationResponse> response = new PageImpl<>(
                responseMapper.toResponses(result.items()), pageable, result.totalElements());
        return APIResponse.<Page<NotificationResponse>>builder()
                .message("All notifications retrieved")
                .result(response)
                .build();
    }

    @GetMapping("/my")
    public APIResponse<List<NotificationResponse>> getAdminNotifications() {
        return APIResponse.<List<NotificationResponse>>builder()
                .message("Admin notifications retrieved")
                .result(responseMapper.toResponses(adminNotificationUseCase.getAdminNotifications()))
                .build();
    }

    @GetMapping("/unread-count")
    public APIResponse<Long> getUnreadCount() {
        return APIResponse.<Long>builder()
                .message("Unread count retrieved")
                .result(adminNotificationUseCase.getUnreadCount())
                .build();
    }

    @PostMapping("/send")
    public APIResponse<Void> sendNotification(@RequestBody @Valid AdminSendNotificationRequest request) {
        adminNotificationUseCase.sendNotification(new AdminSendNotificationCommand(
                request.userId(), request.roleName(), request.title(), request.content(), request.type()));
        return APIResponse.<Void>builder().message("Notification sent successfully").build();
    }

    @PutMapping("/{id}/read")
    public APIResponse<Void> markAsRead(@PathVariable UUID id) {
        adminNotificationUseCase.markAsRead(id);
        return APIResponse.<Void>builder().message("Notification marked as read").build();
    }

    @PutMapping("/read-all")
    public APIResponse<Void> markAllAsRead() {
        adminNotificationUseCase.markAllAsRead();
        return APIResponse.<Void>builder().message("All notifications marked as read").build();
    }

    @DeleteMapping("/{id}")
    public APIResponse<Void> deleteNotification(@PathVariable UUID id) {
        adminNotificationUseCase.deleteNotification(id);
        return APIResponse.<Void>builder().message("Notification deleted").build();
    }
}
