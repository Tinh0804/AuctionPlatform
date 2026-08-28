package com.ecommerce.auctionplatform.auction.application.port.out;

import java.time.LocalDate;
import java.util.UUID;

public record AuctionUserView(
        UUID id,
        String name,
        LocalDate dateOfBirth,
        boolean verified,
        int reputationScore
) {
}
