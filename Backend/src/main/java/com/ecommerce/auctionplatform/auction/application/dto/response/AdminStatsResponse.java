package com.ecommerce.auctionplatform.auction.application.dto.response;
import com.ecommerce.auctionplatform.payment.application.dto.response.RevenueChartData;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminStatsResponse {
    long totalUsers;
    long totalAuctions;
    long totalOrders;
    long totalDisputes;
    BigDecimal totalRevenue;
    List<RevenueChartData> chartData;
}
