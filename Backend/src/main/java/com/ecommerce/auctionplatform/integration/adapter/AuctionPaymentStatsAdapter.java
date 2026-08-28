package com.ecommerce.auctionplatform.integration.adapter;

import com.ecommerce.auctionplatform.auction.application.dto.response.RevenueChartData;
import com.ecommerce.auctionplatform.auction.application.port.out.PaymentStatsPort;
import com.ecommerce.auctionplatform.payment.application.dto.response.PaymentRevenuePoint;
import com.ecommerce.auctionplatform.payment.application.port.in.PaymentStatisticsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuctionPaymentStatsAdapter implements PaymentStatsPort {
    private final PaymentStatisticsUseCase paymentStatisticsUseCase;

    @Override
    public long countOrders() {
        return paymentStatisticsUseCase.countOrders();
    }

    @Override
    public BigDecimal getTotalPlatformFeeRevenue() {
        return paymentStatisticsUseCase.getTotalPlatformFeeRevenue();
    }

    @Override
    public List<RevenueChartData> getRevenueGroupedByMonth(LocalDateTime startDate) {
        return map(paymentStatisticsUseCase.getRevenueGroupedByMonth(startDate));
    }

    @Override
    public List<RevenueChartData> getRevenueGroupedByDay(LocalDateTime startDate) {
        return map(paymentStatisticsUseCase.getRevenueGroupedByDay(startDate));
    }

    private List<RevenueChartData> map(List<PaymentRevenuePoint> points) {
        return points.stream()
                .map(point -> RevenueChartData.builder()
                        .date(point.date())
                        .revenue(point.revenue())
                        .build())
                .toList();
    }
}
