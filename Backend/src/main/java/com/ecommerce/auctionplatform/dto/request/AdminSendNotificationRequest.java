package com.ecommerce.auctionplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminSendNotificationRequest {
    UUID userId;        // null = broadcast cho tất cả user hoặc theo roleName
    String roleName;    // "ADMIN" hoặc "USER"

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự")
    String title;

    @NotBlank(message = "Nội dung không được để trống")
    @Size(max = 1000, message = "Nội dung tối đa 1000 ký tự")
    String content;

    String type;        // "SYSTEM", "ANNOUNCEMENT", "WARNING", "INFO" — default ADMIN_ANNOUNCEMENT
}
