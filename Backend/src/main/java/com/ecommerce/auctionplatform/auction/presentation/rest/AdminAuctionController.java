package com.ecommerce.auctionplatform.auction.presentation.rest;

import com.ecommerce.auctionplatform.auction.application.dto.command.AdminUpdateAuctionCommand;
import com.ecommerce.auctionplatform.auction.application.port.in.AdminAuctionUseCase;
import com.ecommerce.auctionplatform.auction.presentation.dto.request.AdminAuctionUpdateRequest;
import com.ecommerce.auctionplatform.auction.presentation.dto.response.AuctionResponse;
import com.ecommerce.auctionplatform.auction.presentation.mapper.AuctionResponseMapper;
import com.ecommerce.auctionplatform.shared.application.model.PageQuery;
import com.ecommerce.auctionplatform.shared.domain.model.PageResult;
import com.ecommerce.auctionplatform.shared.presentation.response.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/auctions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuctionController {
    private final AdminAuctionUseCase adminAuctionUseCase;
    private final AuctionResponseMapper responseMapper;

    @GetMapping
    public APIResponse<Page<AuctionResponse>> getAllAuctions(
            @RequestParam(required = false) String status,
            @RequestParam(name = "category_id", required = false) String categoryId,
            @PageableDefault(size = 12, sort = "createdAt") Pageable pageable
    ) {
        var sortOrder = pageable.getSort().stream().findFirst();
        var result = adminAuctionUseCase.getAllAuctions(status, categoryId, new PageQuery(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        sortOrder.map(order -> order.getProperty()).orElse("createdAt"),
                        sortOrder.map(order -> order.isAscending()).orElse(false)));
        Pageable responsePageable = PageRequest.of(result.pageNumber(), result.pageSize(), pageable.getSort());
        Page<AuctionResponse> page = new PageImpl<>(
                responseMapper.toAuctionResponses(result.items()), responsePageable, result.totalElements());
        return APIResponse.<Page<AuctionResponse>>builder()
                .message("Auctions fetched successfully")
                .result(page)
                .build();
    }

    @PutMapping("/{id}/status")
    public APIResponse<Void> updateAuctionStatus(@PathVariable UUID id, @RequestParam String status) {
        adminAuctionUseCase.updateAuctionStatus(id, status);
        return APIResponse.<Void>builder().message("Auction status updated successfully").build();
    }

    @PutMapping("/{id}")
    public APIResponse<Void> updateAuction(
            @PathVariable UUID id,
            @RequestBody AdminAuctionUpdateRequest request
    ) {
        UUID categoryId = request.categoryId() == null || request.categoryId().isBlank()
                ? null
                : UUID.fromString(request.categoryId());
        adminAuctionUseCase.updateAuction(id, new AdminUpdateAuctionCommand(
                request.name(), request.description(), request.origin(), categoryId,
                request.condition(), request.manufactureYear(), request.startPrice(),
                request.stepPrice(), request.depositAmount(), request.startTime(), request.endTime()));
        return APIResponse.<Void>builder().message("Auction updated successfully").build();
    }

    @DeleteMapping("/{id}")
    public APIResponse<Void> deleteAuction(@PathVariable UUID id) {
        adminAuctionUseCase.deleteAuction(id);
        return APIResponse.<Void>builder().message("Auction deleted successfully").build();
    }
}
