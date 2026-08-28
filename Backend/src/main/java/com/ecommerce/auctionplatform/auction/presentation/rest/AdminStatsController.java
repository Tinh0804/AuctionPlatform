package com.ecommerce.auctionplatform.auction.presentation.rest;

import com.ecommerce.auctionplatform.shared.presentation.response.APIResponse;
import com.ecommerce.auctionplatform.auction.application.port.in.AdminStatsUseCase;
import com.ecommerce.auctionplatform.auction.presentation.dto.response.AdminStatsResponse;
import com.ecommerce.auctionplatform.auction.presentation.mapper.AuctionResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsUseCase adminStatsService;
    private final AuctionResponseMapper responseMapper;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public APIResponse<AdminStatsResponse> getOverviewStats(
            @RequestParam(value = "period", defaultValue = "week") String period) {
        return APIResponse.<AdminStatsResponse>builder()
                .result(responseMapper.toAdminStatsResponse(adminStatsService.getOverviewStats(period)))
                .message("Dashboard stats fetched successfully")
                .build();
    }
}
