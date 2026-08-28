package com.ecommerce.auctionplatform.identity.application.dto.command;

import java.time.LocalDate;

public record AdminUpdateUserCommand(
        String name,
        String phone,
        String email,
        String identityCard,
        Boolean gender,
        LocalDate dob,
        Integer reputationScore
) {
}
