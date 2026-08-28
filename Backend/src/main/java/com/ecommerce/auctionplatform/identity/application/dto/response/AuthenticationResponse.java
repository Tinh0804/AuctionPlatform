package com.ecommerce.auctionplatform.identity.application.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticationResponse {
    String token;
    String refreshToken;
    AccountResponse account;
}
