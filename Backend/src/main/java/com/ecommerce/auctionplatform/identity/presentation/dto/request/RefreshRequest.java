package com.ecommerce.auctionplatform.identity.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(@NotBlank String token, @NotBlank String refreshToken) {
}
