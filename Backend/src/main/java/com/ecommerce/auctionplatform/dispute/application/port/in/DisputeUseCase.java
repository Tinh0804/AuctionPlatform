package com.ecommerce.auctionplatform.dispute.application.port.in;

import com.ecommerce.auctionplatform.dispute.application.dto.command.CreateDisputeCommand;
import com.ecommerce.auctionplatform.dispute.application.dto.command.ResolveDisputeCommand;
import com.ecommerce.auctionplatform.dispute.application.dto.response.DisputeResponse;
import com.ecommerce.auctionplatform.shared.application.model.FileContent;

import java.util.List;
import java.util.UUID;

/**
 * Port/In – Use case interface for Dispute domain.
 */
public interface DisputeUseCase {

    DisputeResponse createDispute(CreateDisputeCommand request, List<FileContent> files);

    List<DisputeResponse> getMyDisputes();

    List<DisputeResponse> getAllDisputes();

    DisputeResponse getDisputeDetail(UUID disputeId);

    DisputeResponse resolveDispute(UUID disputeId, ResolveDisputeCommand request);
}
