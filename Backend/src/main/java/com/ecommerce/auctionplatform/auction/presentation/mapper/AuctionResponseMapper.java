package com.ecommerce.auctionplatform.auction.presentation.mapper;

import com.ecommerce.auctionplatform.auction.presentation.dto.response.AdminStatsResponse;
import com.ecommerce.auctionplatform.auction.presentation.dto.response.AuctionCreationResponse;
import com.ecommerce.auctionplatform.auction.presentation.dto.response.AuctionDetailResponse;
import com.ecommerce.auctionplatform.auction.presentation.dto.response.AuctionImageResponse;
import com.ecommerce.auctionplatform.auction.presentation.dto.response.AuctionResponse;
import com.ecommerce.auctionplatform.auction.presentation.dto.response.BidResponse;
import com.ecommerce.auctionplatform.auction.presentation.dto.response.RevenueChartData;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AuctionResponseMapper {
    AuctionResponse toAuctionResponse(
            com.ecommerce.auctionplatform.auction.application.dto.response.AuctionResponse source);

    List<AuctionResponse> toAuctionResponses(
            List<com.ecommerce.auctionplatform.auction.application.dto.response.AuctionResponse> source);

    AuctionCreationResponse toCreationResponse(
            com.ecommerce.auctionplatform.auction.application.dto.response.AuctionCreationResponse source);

    AuctionDetailResponse toDetailResponse(
            com.ecommerce.auctionplatform.auction.application.dto.response.AuctionDetailResponse source);

    AuctionImageResponse toImageResponse(
            com.ecommerce.auctionplatform.auction.application.dto.response.AuctionImageResponse source);

    BidResponse toBidResponse(
            com.ecommerce.auctionplatform.auction.application.dto.response.BidResponse source);

    List<BidResponse> toBidResponses(
            List<com.ecommerce.auctionplatform.auction.application.dto.response.BidResponse> source);

    AdminStatsResponse toAdminStatsResponse(
            com.ecommerce.auctionplatform.auction.application.dto.response.AdminStatsResponse source);

    RevenueChartData toRevenueChartData(
            com.ecommerce.auctionplatform.auction.application.dto.response.RevenueChartData source);
}
