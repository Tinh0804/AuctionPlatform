package com.ecommerce.auctionplatform.identity.presentation.dto.response;

public record AuthenticationResponse(
        String token,
        String refreshToken,
        AccountResponse account
) {
}
