package com.ecommerce.auctionplatform.auction.application.dto.response;

import lombok.Builder;

@Builder
public record AuctionImageResponse(String url, Boolean isCover) {
}
