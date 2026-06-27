package com.ecommerce.auctionplatform.controller;

import com.ecommerce.auctionplatform.dto.request.AuctionCreationRequest;
import com.ecommerce.auctionplatform.dto.request.BidRequest;
import com.ecommerce.auctionplatform.dto.respose.APIResponse;
import com.ecommerce.auctionplatform.dto.respose.AuctionCreationResponse;
import com.ecommerce.auctionplatform.dto.respose.AuctionDetailResponse;
import com.ecommerce.auctionplatform.dto.respose.BidResponse;
import com.ecommerce.auctionplatform.dto.respose.CategoryResponse;
import com.ecommerce.auctionplatform.service.AuctionService;
import com.ecommerce.auctionplatform.service.CategoryService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;
    private final CategoryService categoryService;

    @GetMapping
    public APIResponse<Page<AuctionDetailResponse>> getAllAuctions(
            @RequestParam(required = false) String status,
            @RequestParam(name = "category_id", required = false) String categoryId,
            @PageableDefault(size = 12) Pageable pageable) {
        return APIResponse.<Page<AuctionDetailResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Auctions fetched successfully")
                .result(auctionService.getAllAuctions(status, categoryId, pageable))
                .build();
    }

    @GetMapping("/categories")
    public APIResponse<List<CategoryResponse>> getCategories() {
        return APIResponse.<List<CategoryResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Categories fetched successfully")
                .result(categoryService.getAllCategories())
                .build();
    }

    @PostMapping("/create-auction")
    public APIResponse<AuctionCreationResponse> createAuction(@ModelAttribute AuctionCreationRequest request) throws IOException {

        AuctionCreationResponse response = auctionService.createAuction(request);

        return APIResponse.<AuctionCreationResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Auction created successfully")
                .result(response)
                .build();
    }
    @GetMapping("/{id}")
    public APIResponse<AuctionDetailResponse> getAuctionDetail(@PathVariable UUID id) {
        return APIResponse.<AuctionDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Auction detail fetched successfully")
                .result(auctionService.getAuctionDetail(id))
                .build();
    }

    @GetMapping("/{id}/bids")
    public APIResponse<List<BidResponse>> getAuctionBids(@PathVariable UUID id) {
        return APIResponse.<List<BidResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Auction bids fetched successfully")
                .result(auctionService.getAuctionBids(id))
                .build();
    }

    @PostMapping("/{id}/bid")
    public APIResponse<BidResponse> placeBid(
            @PathVariable UUID id,
            @RequestBody BidRequest request) {
        return APIResponse.<BidResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Bid placed successfully")
                .result(auctionService.placeBid(id, request))
                .build();
    }
}
