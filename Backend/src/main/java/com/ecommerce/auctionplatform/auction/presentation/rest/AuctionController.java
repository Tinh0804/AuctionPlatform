package com.ecommerce.auctionplatform.auction.presentation.rest;

import com.ecommerce.auctionplatform.auction.application.dto.command.CreateAuctionCommand;
import com.ecommerce.auctionplatform.auction.application.dto.command.PlaceBidCommand;
import com.ecommerce.auctionplatform.auction.presentation.dto.request.AuctionCreationRequest;
import com.ecommerce.auctionplatform.auction.presentation.dto.request.BidRequest;
import com.ecommerce.auctionplatform.auction.presentation.dto.response.AuctionCreationResponse;
import com.ecommerce.auctionplatform.auction.presentation.dto.response.AuctionDetailResponse;
import com.ecommerce.auctionplatform.auction.presentation.dto.response.AuctionResponse;
import com.ecommerce.auctionplatform.auction.presentation.dto.response.BidResponse;
import com.ecommerce.auctionplatform.auction.presentation.mapper.AuctionResponseMapper;
import com.ecommerce.auctionplatform.shared.presentation.response.APIResponse;
import com.ecommerce.auctionplatform.auction.application.port.in.AuctionUseCase;
import com.ecommerce.auctionplatform.shared.application.model.PageQuery;
import com.ecommerce.auctionplatform.shared.presentation.mapper.FileUploadMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionUseCase auctionService;
    private final AuctionResponseMapper responseMapper;

    @GetMapping
    public APIResponse<Page<AuctionResponse>> getAllAuctions(
            @RequestParam(required = false) String status,
            @RequestParam(name = "category_id", required = false) String categoryId,
            @PageableDefault(size = 12) Pageable pageable) {
        var result = auctionService.getAllAuctions(
                status,
                categoryId,
                new PageQuery(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSort().stream().findFirst().map(sort -> sort.getProperty()).orElse("createdAt"),
                        pageable.getSort().stream().findFirst().map(sort -> sort.isAscending()).orElse(false)
                ));

        return APIResponse.<Page<AuctionResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Auctions fetched successfully")
                .result(toSpringPage(
                        responseMapper.toAuctionResponses(result.items()),
                        result.totalElements(),
                        pageable))
                .build();
    }

    @PostMapping("/create-auction")
    public APIResponse<AuctionCreationResponse> createAuction(@ModelAttribute AuctionCreationRequest request) {

        AuctionCreationResponse response = responseMapper.toCreationResponse(
                auctionService.createAuction(toCommand(request)));

        return APIResponse.<AuctionCreationResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Auction created successfully")
                .result(response)
                .build();
    }

    private CreateAuctionCommand toCommand(AuctionCreationRequest request) {
        return CreateAuctionCommand.builder()
                .name(request.getName())
                .description(request.getDescription())
                .origin(request.getOrigin())
                .categoryId(request.getCategoryId())
                .condition(request.getCondition())
                .startPrice(request.getStartPrice())
                .stepPrice(request.getStepPrice())
                .depositAmount(request.getDepositAmount())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .reservePrice(request.getReservePrice())
                .buyNowPrice(request.getBuyNowPrice())
                .autoExtend(request.getAutoExtend())
                .extendMinutes(request.getExtendMinutes())
                .relistId(request.getRelistId())
                .files(FileUploadMapper.toContents(request.getFiles()))
                .build();
    }

    private Page<AuctionResponse> toSpringPage(
            List<AuctionResponse> items,
            long totalElements,
            Pageable pageable) {
        return new PageImpl<>(items, pageable, totalElements);
    }

    @GetMapping("/{id}")
    public APIResponse<AuctionDetailResponse> getAuctionDetail(@PathVariable UUID id) {
        return APIResponse.<AuctionDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Auction detail fetched successfully")
                .result(responseMapper.toDetailResponse(auctionService.getAuctionDetail(id)))
                .build();
    }

    @GetMapping("/{id}/bids")
    public APIResponse<List<BidResponse>> getAuctionBids(@PathVariable UUID id) {
        return APIResponse.<List<BidResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Auction bids fetched successfully")
                .result(responseMapper.toBidResponses(auctionService.getAuctionBids(id)))
                .build();
    }

    @PostMapping("/{id}/bid")
    public APIResponse<BidResponse> placeBid(
            @PathVariable UUID id,
            @RequestBody @Valid BidRequest request) {
        return APIResponse.<BidResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Bid placed successfully")
                .result(responseMapper.toBidResponse(
                        auctionService.placeBid(id, PlaceBidCommand.builder()
                                .bidAmount(request.bidAmount())
                                .build())))
                .build();
    }
}
