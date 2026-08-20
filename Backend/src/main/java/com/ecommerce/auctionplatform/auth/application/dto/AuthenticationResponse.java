package com.ecommerce.auctionplatform.auth.application.dto;

import lombok.Builder;
import lombok.Data;
import com.ecommerce.auctionplatform.user.application.dto.response.AccountResponse;

@Data
@Builder
public class AuthenticationResponse {
    String token;
    String refreshToken;
    AccountResponse account;
}
