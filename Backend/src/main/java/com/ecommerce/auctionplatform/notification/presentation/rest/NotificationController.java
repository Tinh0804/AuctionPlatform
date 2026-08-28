package com.ecommerce.auctionplatform.notification.presentation.rest;

import com.ecommerce.auctionplatform.shared.presentation.response.APIResponse;
import com.ecommerce.auctionplatform.notification.application.port.in.NotificationUseCase;
import com.ecommerce.auctionplatform.notification.presentation.dto.response.NotificationResponse;
import com.ecommerce.auctionplatform.notification.presentation.mapper.NotificationResponseMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/notifications")
public class NotificationController {

    NotificationUseCase notificationService;
    NotificationResponseMapper responseMapper;

    @GetMapping("/my")
    public APIResponse<List<NotificationResponse>> getMyNotifications() {
        return APIResponse.<List<NotificationResponse>>builder()
                .message("Notifications retrieved successfully")
                .result(responseMapper.toResponses(notificationService.getMyNotifications()))
                .build();
    }

    @PostMapping("/{id}/read")
    public APIResponse<Void> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id);
        return APIResponse.<Void>builder()
                .message("Notification marked as read")
                .build();
    }
}
