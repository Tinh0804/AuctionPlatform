package com.ecommerce.auctionplatform.product.presentation.dto.response;

import java.util.UUID;

public record CategoryResponse(UUID id, String name, String description) {
}
