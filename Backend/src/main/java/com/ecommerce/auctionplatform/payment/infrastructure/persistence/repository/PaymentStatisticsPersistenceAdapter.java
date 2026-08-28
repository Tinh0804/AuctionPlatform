package com.ecommerce.auctionplatform.payment.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.payment.application.dto.response.PaymentRevenuePoint;
import com.ecommerce.auctionplatform.payment.application.port.out.PaymentStatisticsQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentStatisticsPersistenceAdapter implements PaymentStatisticsQueryPort {
    private final OrderJpaRepository orderJpaRepository;
    private final TransactionJpaRepository transactionJpaRepository;

    @Override
    public long countOrders() {
        return orderJpaRepository.count();
    }

    @Override
    public BigDecimal getTotalPlatformFeeRevenue() {
        BigDecimal revenue = transactionJpaRepository.getTotalPlatformFeeRevenue();
        return revenue == null ? BigDecimal.ZERO : revenue;
    }

    @Override
    public List<PaymentRevenuePoint> getRevenueGroupedByMonth(LocalDateTime startDate) {
        return map(transactionJpaRepository.getRevenueGroupedByMonthNative(startDate));
    }

    @Override
    public List<PaymentRevenuePoint> getRevenueGroupedByDay(LocalDateTime startDate) {
        return map(transactionJpaRepository.getRevenueGroupedByDayNative(startDate));
    }

    private List<PaymentRevenuePoint> map(List<Object[]> rows) {
        return rows.stream()
                .filter(row -> row.length >= 2 && row[1] != null)
                .map(row -> new PaymentRevenuePoint(
                        String.valueOf(row[0]),
                        new BigDecimal(String.valueOf(row[1]))))
                .toList();
    }
}
