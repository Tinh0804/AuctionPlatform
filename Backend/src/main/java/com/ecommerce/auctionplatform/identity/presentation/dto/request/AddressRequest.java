package com.ecommerce.auctionplatform.identity.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
        @NotBlank String ward,
        @NotBlank String district,
        @NotBlank String city,
        @NotBlank String addressLine,
        Boolean isDefault
) {
}
