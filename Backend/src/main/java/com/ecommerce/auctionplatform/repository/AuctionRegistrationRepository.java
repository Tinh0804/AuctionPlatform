package com.ecommerce.auctionplatform.repository;

import com.ecommerce.auctionplatform.entity.AuctionRegistration;
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
