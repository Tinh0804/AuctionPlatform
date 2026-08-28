package com.ecommerce.auctionplatform.identity.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String userName, @NotBlank String passWord) {
}
