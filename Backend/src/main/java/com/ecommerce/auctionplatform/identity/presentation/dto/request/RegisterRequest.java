package com.ecommerce.auctionplatform.identity.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3) String userName,
        @NotBlank @Size(min = 5) String passWord,
        @NotBlank String fullName,
        @NotBlank String phone,
        @NotBlank @Email String email
) {
}
