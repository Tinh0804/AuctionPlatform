package com.ecommerce.auctionplatform.auction.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.auction.infrastructure.persistence.entity.AuctionEntity;
import com.ecommerce.auctionplatform.auction.domain.enums.AuctionStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.time.LocalDateTime;

@Repository
interface AuctionJpaRepository extends JpaRepository<AuctionEntity, UUID>, JpaSpecificationExecutor<AuctionEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AuctionEntity a WHERE a.id = :id")
    Optional<AuctionEntity> findByIdWithLock(@Param("id") UUID id);
    
    List<AuctionEntity> findByStatusInAndStartTimeBefore(List<AuctionStatus> statuses, LocalDateTime time);
    List<AuctionEntity> findByStatusInAndEndTimeBefore(List<AuctionStatus> statuses, LocalDateTime time);

    @Query("SELECT a FROM AuctionEntity a WHERE a.status = :status")
    Page<AuctionEntity> searchByStatus(@Param("status") AuctionStatus status, Pageable pageable);

    @Query("""
            SELECT a FROM AuctionEntity a
            WHERE EXISTS (SELECT p.id FROM ProductEntity p
                          WHERE p.id = a.productId AND p.category.id = :categoryId)
            """)
    Page<AuctionEntity> searchByCategory(@Param("categoryId") UUID categoryId, Pageable pageable);

    @Query("""
            SELECT a FROM AuctionEntity a
            WHERE a.status = :status
              AND EXISTS (SELECT p.id FROM ProductEntity p
                          WHERE p.id = a.productId AND p.category.id = :categoryId)
            """)
    Page<AuctionEntity> searchByStatusAndCategory(
            @Param("status") AuctionStatus status,
            @Param("categoryId") UUID categoryId,
            Pageable pageable);
}
