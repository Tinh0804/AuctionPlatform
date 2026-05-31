package com.ecommerce.auctionplatform.dto.respose;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticationResponse {
    String token;
    String refreshToken;
    AccountResponse account;
}
