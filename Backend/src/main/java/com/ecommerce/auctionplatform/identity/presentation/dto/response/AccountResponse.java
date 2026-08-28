package com.ecommerce.auctionplatform.identity.presentation.dto.response;

import java.util.UUID;

public record AccountResponse(
        UUID id,
        String username,
        String email,
        String role,
        String providerType,
        Boolean active
) {
}
