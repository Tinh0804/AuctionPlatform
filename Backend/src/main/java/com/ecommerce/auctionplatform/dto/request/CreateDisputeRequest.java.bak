package com.ecommerce.auctionplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateDisputeRequest {
    @NotNull(message = "Order ID is required")
    UUID orderId;

    @NotBlank(message = "Reason is required")
    @Size(max = 255, message = "Reason must not exceed 255 characters")
    String reason;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    String description;
    
    // Ảnh sẽ upload qua MultipartFile[], không nằm trong DTO JSON
}
