package com.ecommerce.auctionplatform.auction.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.auction.infrastructure.persistence.entity.AuctionRegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface AuctionRegistrationJpaRepository extends JpaRepository<AuctionRegistrationEntity, UUID> {
    Optional<AuctionRegistrationEntity> findByAuctionIdAndUserId(UUID auctionId, UUID userId);
    List<AuctionRegistrationEntity> findByAuctionId(UUID auctionId);
}
