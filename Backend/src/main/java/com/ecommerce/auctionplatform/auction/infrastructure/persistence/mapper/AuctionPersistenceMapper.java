package com.ecommerce.auctionplatform.auction.infrastructure.persistence.mapper;

import com.ecommerce.auctionplatform.auction.domain.model.*;
import com.ecommerce.auctionplatform.auction.infrastructure.persistence.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AuctionPersistenceMapper {
    Auction toDomain(AuctionEntity entity);
    AuctionEntity toEntity(Auction domain);

    Bid toDomain(BidEntity entity);
    BidEntity toEntity(Bid domain);

    AuctionRecord toDomain(AuctionRecordEntity entity);
    AuctionRecordEntity toEntity(AuctionRecord domain);

    AuctionRegistration toDomain(AuctionRegistrationEntity entity);
    AuctionRegistrationEntity toEntity(AuctionRegistration domain);
}
