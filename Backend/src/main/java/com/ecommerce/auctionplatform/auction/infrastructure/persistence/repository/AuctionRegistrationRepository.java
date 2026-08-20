package com.ecommerce.auctionplatform.auction.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.auction.domain.model.AuctionRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuctionRegistrationRepository extends JpaRepository<AuctionRegistration, UUID> {
    Optional<AuctionRegistration> findByAuctionIdAndUserId(UUID auctionId, UUID userId);
    List<AuctionRegistration> findByAuctionId(UUID auctionId);
}
