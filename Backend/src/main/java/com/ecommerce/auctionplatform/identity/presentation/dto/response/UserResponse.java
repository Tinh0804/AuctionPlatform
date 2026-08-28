package com.ecommerce.auctionplatform.identity.presentation.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        AccountResponse account,
        String name,
        String phone,
        String email,
        String identityCard,
        Boolean gender,
        Integer reputationScore,
        String verificationStatus,
        String identityFrontImage,
        String identityBackImage,
        String avatarImage,
        LocalDate dob,
        List<AddressResponse> addresses,
        WalletResponse wallet
) {
}
