package com.ecommerce.auctionplatform.dispute.presentation.mapper;

import com.ecommerce.auctionplatform.dispute.presentation.dto.response.DisputeResponse;
import com.ecommerce.auctionplatform.dispute.presentation.dto.response.EvidenceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DisputeResponseMapper {
    DisputeResponse toResponse(
            com.ecommerce.auctionplatform.dispute.application.dto.response.DisputeResponse source);

    List<DisputeResponse> toResponses(
            List<com.ecommerce.auctionplatform.dispute.application.dto.response.DisputeResponse> source);

    EvidenceResponse toEvidenceResponse(
            com.ecommerce.auctionplatform.dispute.application.dto.response.EvidenceResponse source);
}
