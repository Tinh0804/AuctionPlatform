package com.ecommerce.auctionplatform.identity.presentation.dto.response;

import java.util.UUID;

public record AddressResponse(
        UUID id,
        String ward,
        String district,
        String city,
        String addressLine,
        Boolean isDefault
) {
}
