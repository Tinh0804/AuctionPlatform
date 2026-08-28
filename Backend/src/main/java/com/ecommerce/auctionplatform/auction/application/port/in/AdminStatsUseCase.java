package com.ecommerce.auctionplatform.auction.application.port.in;

import com.ecommerce.auctionplatform.auction.application.dto.response.AdminStatsResponse;

public interface AdminStatsUseCase {
    AdminStatsResponse getOverviewStats(String period);
}
