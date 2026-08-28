package com.ecommerce.auctionplatform.auction.application.port.in;

import com.ecommerce.auctionplatform.auction.application.dto.response.AuctionCreationResponse;
import com.ecommerce.auctionplatform.auction.application.dto.response.AuctionDetailResponse;
import com.ecommerce.auctionplatform.auction.application.dto.response.AuctionResponse;
import com.ecommerce.auctionplatform.auction.application.dto.response.BidResponse;
import com.ecommerce.auctionplatform.auction.application.dto.command.CreateAuctionCommand;
import com.ecommerce.auctionplatform.auction.application.dto.command.PlaceBidCommand;
import com.ecommerce.auctionplatform.shared.application.model.PageQuery;
import com.ecommerce.auctionplatform.shared.domain.model.PageResult;

import java.util.List;
import java.util.UUID;

public interface AuctionUseCase {

    AuctionCreationResponse createAuction(CreateAuctionCommand command);

    PageResult<AuctionResponse> getAllAuctions(String status, String categoryId, PageQuery pageQuery);

    AuctionDetailResponse getAuctionDetail(UUID id);

    List<BidResponse> getAuctionBids(UUID id);

    BidResponse placeBid(UUID auctionId, PlaceBidCommand request);

    void activateAuction(String auctionId);

    void closeAuction(String auctionId);

    void handlePaymentExpiry(UUID recordId);

    void processAllStuckEntities();
}
