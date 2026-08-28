package com.ecommerce.auctionplatform.auction.presentation.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record AdminStatsResponse(
        long totalUsers,
        long totalAuctions,
        long totalOrders,
        long totalDisputes,
        BigDecimal totalRevenue,
        List<RevenueChartData> chartData
) {
}
