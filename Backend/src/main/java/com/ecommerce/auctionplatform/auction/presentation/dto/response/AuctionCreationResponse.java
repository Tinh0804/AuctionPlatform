package com.ecommerce.auctionplatform.auction.presentation.dto.response;

import java.util.UUID;

public record AuctionCreationResponse(UUID auctionId, String message) {
}
