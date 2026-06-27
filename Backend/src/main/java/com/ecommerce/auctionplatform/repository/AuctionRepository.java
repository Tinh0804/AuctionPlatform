package com.ecommerce.auctionplatform.repository;

import com.ecommerce.auctionplatform.entity.Auction;
import com.ecommerce.auctionplatform.entity.enums.AuctionStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, UUID>, JpaSpecificationExecutor<Auction> {
}
