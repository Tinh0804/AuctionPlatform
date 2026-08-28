package com.ecommerce.auctionplatform.payment.application.service;

import com.ecommerce.auctionplatform.payment.application.dto.response.PaymentRevenuePoint;
import com.ecommerce.auctionplatform.payment.application.port.in.PaymentStatisticsUseCase;
import com.ecommerce.auctionplatform.payment.application.port.out.PaymentStatisticsQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentStatisticsService implements PaymentStatisticsUseCase {
    private final PaymentStatisticsQueryPort statisticsQueryPort;

    @Override
    public long countOrders() {
        return statisticsQueryPort.countOrders();
    }

    @Override
    public BigDecimal getTotalPlatformFeeRevenue() {
        return statisticsQueryPort.getTotalPlatformFeeRevenue();
    }

    @Override
    public List<PaymentRevenuePoint> getRevenueGroupedByMonth(LocalDateTime startDate) {
        return statisticsQueryPort.getRevenueGroupedByMonth(startDate);
    }

    @Override
    public List<PaymentRevenuePoint> getRevenueGroupedByDay(LocalDateTime startDate) {
        return statisticsQueryPort.getRevenueGroupedByDay(startDate);
    }
}
