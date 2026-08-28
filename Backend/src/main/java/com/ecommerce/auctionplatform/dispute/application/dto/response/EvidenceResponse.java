package com.ecommerce.auctionplatform.dispute.application.dto.response;

import lombok.Builder;

@Builder
public record EvidenceResponse(String url, Boolean isCover) {
}
