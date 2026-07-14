package com.ecommerce.auctionplatform.repository;

import com.ecommerce.auctionplatform.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByWalletIdOrderByCreatedAtDesc(UUID walletId);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = 'PLATFORM_FEE'")
    java.math.BigDecimal getTotalPlatformFeeRevenue();

    @Query(value = 
        "SELECT TO_CHAR(created_at, 'YYYY-MM-DD') as date, SUM(amount) as revenue " +
        "FROM transactions " +
        "WHERE type = 'PLATFORM_FEE' AND created_at >= :startDate " +
        "GROUP BY TO_CHAR(created_at, 'YYYY-MM-DD') " +
        "ORDER BY date ASC", nativeQuery = true)
    List<Object[]> getRevenueGroupedByDayNative(@Param("startDate") java.time.LocalDateTime startDate);

    @Query(value = 
        "SELECT TO_CHAR(created_at, 'YYYY-MM') as date, SUM(amount) as revenue " +
        "FROM transactions " +
        "WHERE type = 'PLATFORM_FEE' AND created_at >= :startDate " +
        "GROUP BY TO_CHAR(created_at, 'YYYY-MM') " +
        "ORDER BY date ASC", nativeQuery = true)
    List<Object[]> getRevenueGroupedByMonthNative(@Param("startDate") java.time.LocalDateTime startDate);
}
