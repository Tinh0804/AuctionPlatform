package com.ecommerce.auctionplatform.auction.application.port.out;
import com.ecommerce.auctionplatform.auction.application.dto.response.RevenueChartData;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
public interface PaymentStatsPort {
    long countOrders();
    BigDecimal getTotalPlatformFeeRevenue();
    List<RevenueChartData> getRevenueGroupedByMonth(LocalDateTime startDate);
    List<RevenueChartData> getRevenueGroupedByDay(LocalDateTime startDate);
}
