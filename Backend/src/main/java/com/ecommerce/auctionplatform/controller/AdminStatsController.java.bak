package com.ecommerce.auctionplatform.controller;

import com.ecommerce.auctionplatform.dto.respose.APIResponse;
import com.ecommerce.auctionplatform.dto.respose.AdminStatsResponse;
import com.ecommerce.auctionplatform.dto.respose.RevenueChartData;
import com.ecommerce.auctionplatform.entity.enums.PredefinedRole;
import com.ecommerce.auctionplatform.service.AdminStatsService;
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
