package com.ecommerce.auctionplatform.auction.application.service;

import com.ecommerce.auctionplatform.auction.application.dto.response.AdminStatsResponse;
import com.ecommerce.auctionplatform.auction.application.dto.response.RevenueChartData;
import com.ecommerce.auctionplatform.auction.domain.repository.AuctionRepository;
import com.ecommerce.auctionplatform.auction.application.port.out.PaymentStatsPort;

import com.ecommerce.auctionplatform.auction.application.port.out.UserStatsPort;
import com.ecommerce.auctionplatform.auction.application.port.out.DisputeStatsPort;
import com.ecommerce.auctionplatform.auction.application.port.in.AdminStatsUseCase;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminStatsService implements AdminStatsUseCase {

    UserStatsPort userStatsPort;
    AuctionRepository auctionRepository;
    PaymentStatsPort paymentStatsPort;
    DisputeStatsPort disputeStatsPort;



    @Transactional(readOnly = true)
    public AdminStatsResponse getOverviewStats(String period) {
        long totalUsers = userStatsPort.countUsers();
        long totalAuctions = auctionRepository.count();
        long totalOrders = paymentStatsPort.countOrders();
        long totalDisputes = disputeStatsPort.countDisputes();

        BigDecimal totalRevenue = paymentStatsPort.getTotalPlatformFeeRevenue();
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }
        
        List<RevenueChartData> chartData;
        LocalDateTime startDate;

        if ("year".equalsIgnoreCase(period)) {
            startDate = LocalDateTime.now().minusMonths(12);
            chartData = paymentStatsPort.getRevenueGroupedByMonth(startDate);
        } else if ("month".equalsIgnoreCase(period)) {
            startDate = LocalDateTime.now().minusDays(30);
            chartData = paymentStatsPort.getRevenueGroupedByDay(startDate);
        } else {
            // default to week
            startDate = LocalDateTime.now().minusDays(7);
            chartData = paymentStatsPort.getRevenueGroupedByDay(startDate);
        }
    
        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalAuctions(totalAuctions)
                .totalOrders(totalOrders)
                .totalDisputes(totalDisputes)
                .totalRevenue(totalRevenue)
                .chartData(chartData)
                .build();
    }


}
