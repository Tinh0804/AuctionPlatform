package com.ecommerce.auctionplatform.payment.application.port.in;

import com.ecommerce.auctionplatform.payment.application.dto.response.PaymentRevenuePoint;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Public application contract for reporting consumers outside the payment context.
 */
public interface PaymentStatisticsUseCase {
    long countOrders();

    BigDecimal getTotalPlatformFeeRevenue();

    List<PaymentRevenuePoint> getRevenueGroupedByMonth(LocalDateTime startDate);

    List<PaymentRevenuePoint> getRevenueGroupedByDay(LocalDateTime startDate);
}
