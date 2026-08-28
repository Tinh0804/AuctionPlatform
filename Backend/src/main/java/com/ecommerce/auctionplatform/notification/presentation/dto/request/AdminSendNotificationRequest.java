package com.ecommerce.auctionplatform.notification.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AdminSendNotificationRequest(
        UUID userId,
        String roleName,
        @NotBlank(message = "Tiêu đề không được để trống")
        @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự") String title,
        @NotBlank(message = "Nội dung không được để trống")
        @Size(max = 1000, message = "Nội dung tối đa 1000 ký tự") String content,
        String type
) {
}
