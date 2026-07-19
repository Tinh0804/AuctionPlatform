package com.ecommerce.auctionplatform.controller;

import com.ecommerce.auctionplatform.dto.request.AdminAuctionUpdateRequest;
import com.ecommerce.auctionplatform.dto.respose.APIResponse;
import com.ecommerce.auctionplatform.dto.respose.AuctionResponse;
import com.ecommerce.auctionplatform.entity.enums.PredefinedRole;
import com.ecommerce.auctionplatform.service.AuctionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/auctions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
public class AdminAuctionController {
    AuctionService auctionService;

    @GetMapping
    public APIResponse<Page<AuctionResponse>> getAllAuctions(
            @RequestParam(required = false) String status,
            @RequestParam(name = "category_id", required = false) String categoryId,
            @PageableDefault(size = 12) Pageable pageable) {
        return APIResponse.<Page<AuctionResponse>>builder()
                .result(auctionService.getAllAuctions(status, categoryId, pageable))
                .message("Auctions fetched successfully")
                .build();
    }

    @PutMapping("/{id}/status")
    public APIResponse<Void> updateAuctionStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        auctionService.adminUpdateAuctionStatus(id, status);
        return APIResponse.<Void>builder()
                .message("Auction status updated successfully")
                .build();
    }

    @PutMapping("/{id}")
    public APIResponse<Void> updateAuction(
            @PathVariable UUID id,
            @RequestBody AdminAuctionUpdateRequest request) {
        auctionService.adminUpdateAuction(id, request);
        return APIResponse.<Void>builder()
                .message("Auction updated successfully")
                .build();
    }

    @DeleteMapping("/{id}")
    public APIResponse<Void> deleteAuction(@PathVariable UUID id) {
        auctionService.adminDeleteAuction(id);
        return APIResponse.<Void>builder()
                .message("Auction deleted successfully")
                .build();
    }
}
