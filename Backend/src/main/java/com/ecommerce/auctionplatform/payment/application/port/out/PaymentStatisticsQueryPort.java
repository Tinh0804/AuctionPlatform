package com.ecommerce.auctionplatform.payment.application.port.out;

import com.ecommerce.auctionplatform.payment.application.dto.response.PaymentRevenuePoint;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentStatisticsQueryPort {
    long countOrders();

    BigDecimal getTotalPlatformFeeRevenue();

    List<PaymentRevenuePoint> getRevenueGroupedByMonth(LocalDateTime startDate);

    List<PaymentRevenuePoint> getRevenueGroupedByDay(LocalDateTime startDate);
}
