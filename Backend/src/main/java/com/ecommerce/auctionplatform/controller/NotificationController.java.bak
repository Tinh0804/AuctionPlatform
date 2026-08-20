package com.ecommerce.auctionplatform.controller;

import com.ecommerce.auctionplatform.dto.respose.APIResponse;
import com.ecommerce.auctionplatform.dto.respose.NotificationResponse;
import com.ecommerce.auctionplatform.service.NotificationService;
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

    NotificationService notificationService;

    @GetMapping("/my")
    public APIResponse<List<NotificationResponse>> getMyNotifications() {
        return APIResponse.<List<NotificationResponse>>builder()
                .message("Notifications retrieved successfully")
                .result(notificationService.getMyNotifications())
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
