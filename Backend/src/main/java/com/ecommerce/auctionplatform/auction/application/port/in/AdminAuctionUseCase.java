package com.ecommerce.auctionplatform.auction.application.port.in;

import com.ecommerce.auctionplatform.auction.application.dto.command.AdminUpdateAuctionCommand;
import com.ecommerce.auctionplatform.auction.application.dto.response.AuctionResponse;
import com.ecommerce.auctionplatform.shared.application.model.PageQuery;
import com.ecommerce.auctionplatform.shared.domain.model.PageResult;

import java.util.UUID;

public interface AdminAuctionUseCase {
    PageResult<AuctionResponse> getAllAuctions(String status, String categoryId, PageQuery pageQuery);

    void updateAuctionStatus(UUID id, String status);

    void updateAuction(UUID id, AdminUpdateAuctionCommand command);

    void deleteAuction(UUID id);
}
