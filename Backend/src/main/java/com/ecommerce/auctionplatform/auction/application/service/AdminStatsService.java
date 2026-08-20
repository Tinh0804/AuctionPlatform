package com.ecommerce.auctionplatform.auction.application.service;

import com.ecommerce.auctionplatform.auction.application.dto.response.AdminStatsResponse;
import com.ecommerce.auctionplatform.payment.application.dto.response.RevenueChartData;
import com.ecommerce.auctionplatform.user.domain.model.Role;
import com.ecommerce.auctionplatform.user.domain.model.User;
import com.ecommerce.auctionplatform.payment.domain.model.Wallet;
import com.ecommerce.auctionplatform.auction.infrastructure.persistence.repository.AuctionRepository;
import com.ecommerce.auctionplatform.payment.infrastructure.persistence.repository.OrderRepository;
import com.ecommerce.auctionplatform.payment.infrastructure.persistence.repository.TransactionRepository;
import com.ecommerce.auctionplatform.user.infrastructure.persistence.repository.UserRepository;
import com.ecommerce.auctionplatform.payment.infrastructure.persistence.repository.WalletRepository;
import com.ecommerce.auctionplatform.payment.application.service.WalletService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminStatsService {

    UserRepository userRepository;
    AuctionRepository auctionRepository;
    OrderRepository orderRepository;
    TransactionRepository transactionRepository;
    WalletService walletService;


    @Transactional(readOnly = true)
    public AdminStatsResponse getOverviewStats(String period) {
        long totalUsers = userRepository.count();
        long totalAuctions = auctionRepository.count();
        long totalOrders = orderRepository.count();
        long totalDisputes = 0; // Will be updated when Dispute entity is added

        BigDecimal totalRevenue = transactionRepository.getTotalPlatformFeeRevenue();
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }
        
        List<RevenueChartData> chartData = new ArrayList<>();
        List<Object[]> revenueData;
        LocalDateTime startDate;

        if ("year".equalsIgnoreCase(period)) {
            startDate = LocalDateTime.now().minusMonths(12);
            revenueData = transactionRepository.getRevenueGroupedByMonthNative(startDate);
        } else if ("month".equalsIgnoreCase(period)) {
            startDate = LocalDateTime.now().minusDays(30);
            revenueData = transactionRepository.getRevenueGroupedByDayNative(startDate);
        } else {
            // default to week
            startDate = LocalDateTime.now().minusDays(7);
            revenueData = transactionRepository.getRevenueGroupedByDayNative(startDate);
        }
        
        for (Object[] row : revenueData) {
            if (row[1] != null) {
                chartData.add(RevenueChartData.builder()
                        .date(row[0].toString())
                        .revenue(new BigDecimal(row[1].toString()))
                        .build());
            }
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
