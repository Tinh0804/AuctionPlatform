package com.ecommerce.auctionplatform.auction.application.mapper;

import com.ecommerce.auctionplatform.auction.application.dto.response.AuctionResponse;
import com.ecommerce.auctionplatform.auction.application.dto.response.BidResponse;
import com.ecommerce.auctionplatform.auction.domain.model.Auction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AuctionMapper {

    @Mapping(target = "productName", ignore = true)
    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "coverImage", ignore = true)
    @Mapping(target = "bidCount", ignore = true)
    AuctionResponse toAuctionResponse(Auction auction);

    // In AuctionService, we will call toAuctionResponse, then populate the ignored fields manually
    @Mapping(target = "bidderName", ignore = true)
    @Mapping(target = "bidderId", source = "userId")
    BidResponse toBidResponse(com.ecommerce.auctionplatform.auction.domain.model.Bid bid);

    @Mapping(target = "productName", ignore = true)
    @Mapping(target = "sellerName", ignore = true)
    @Mapping(target = "sellerId", source = "userId")
    @Mapping(target = "images", ignore = true)
    com.ecommerce.auctionplatform.auction.application.dto.response.AuctionDetailResponse toAuctionDetailResponse(Auction auction);
}
