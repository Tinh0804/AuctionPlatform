package com.ecommerce.auctionplatform.auction.presentation.rest;

import com.ecommerce.auctionplatform.shared.presentation.response.APIResponse;
import com.ecommerce.auctionplatform.auction.application.dto.response.AdminStatsResponse;
import com.ecommerce.auctionplatform.payment.application.dto.response.RevenueChartData;
import com.ecommerce.auctionplatform.user.domain.enums.PredefinedRole;
import com.ecommerce.auctionplatform.auction.application.service.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping("/overview")
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public APIResponse<AdminStatsResponse> getOverviewStats(
            @RequestParam(value = "period", defaultValue = "week") String period) {
        return APIResponse.<AdminStatsResponse>builder()
                .result(adminStatsService.getOverviewStats(period))
                .message("Dashboard stats fetched successfully")
                .build();
    }
}
