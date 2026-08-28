package com.ecommerce.auctionplatform.identity.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record UserUpdateRequest(
        @NotBlank String name,
        @Email String email,
        Boolean gender,
        LocalDate dob,
        String avatarImage
) {
}
