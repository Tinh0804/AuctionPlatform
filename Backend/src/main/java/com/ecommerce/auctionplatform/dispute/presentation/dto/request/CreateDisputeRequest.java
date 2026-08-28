package com.ecommerce.auctionplatform.dispute.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateDisputeRequest(
        @NotNull UUID orderId,
        @NotBlank @Size(max = 255) String reason,
        @Size(max = 2000) String description
) {
}
