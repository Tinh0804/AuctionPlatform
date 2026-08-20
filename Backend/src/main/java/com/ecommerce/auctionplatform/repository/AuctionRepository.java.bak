package com.ecommerce.auctionplatform.repository;

import com.ecommerce.auctionplatform.entity.Auction;
import com.ecommerce.auctionplatform.entity.enums.AuctionStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, UUID>, JpaSpecificationExecutor<Auction> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Auction a WHERE a.id = :id")
    Optional<Auction> findByIdWithLock(@Param("id") UUID id);
    
    List<Auction> findByStatusInAndStartTimeBefore(List<AuctionStatus> statuses, LocalDateTime time);
    List<Auction> findByStatusInAndEndTimeBefore(List<AuctionStatus> statuses, LocalDateTime time);
}
