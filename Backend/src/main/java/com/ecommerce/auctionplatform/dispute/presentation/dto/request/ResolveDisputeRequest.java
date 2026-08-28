package com.ecommerce.auctionplatform.dispute.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ResolveDisputeRequest(@NotBlank String outcome, @NotBlank String resolution) {
}
