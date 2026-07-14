package com.ecommerce.auctionplatform.service;

import com.ecommerce.auctionplatform.dto.respose.AdminStatsResponse;
import com.ecommerce.auctionplatform.dto.respose.RevenueChartData;
import com.ecommerce.auctionplatform.entity.Role;
import com.ecommerce.auctionplatform.entity.User;
import com.ecommerce.auctionplatform.entity.Wallet;
import com.ecommerce.auctionplatform.repository.AuctionRepository;
import com.ecommerce.auctionplatform.repository.OrderRepository;
import com.ecommerce.auctionplatform.repository.TransactionRepository;
import com.ecommerce.auctionplatform.repository.UserRepository;
import com.ecommerce.auctionplatform.repository.WalletRepository;
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
