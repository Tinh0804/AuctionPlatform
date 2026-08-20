package com.ecommerce.auctionplatform.dispute.presentation.rest;

import com.ecommerce.auctionplatform.dispute.presentation.dto.request.ResolveDisputeRequest;
import com.ecommerce.auctionplatform.shared.presentation.response.APIResponse;
import com.ecommerce.auctionplatform.dispute.application.dto.response.DisputeResponse;
import com.ecommerce.auctionplatform.dispute.application.service.DisputeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/disputes")
@RequiredArgsConstructor
public class AdminDisputeController {

    private final DisputeService disputeService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public APIResponse<List<DisputeResponse>> getAllDisputes() {
        return APIResponse.<List<DisputeResponse>>builder()
                .result(disputeService.getAllDisputes())
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public APIResponse<DisputeResponse> getDisputeDetail(@PathVariable UUID id) {
        return APIResponse.<DisputeResponse>builder()
                .result(disputeService.getDisputeDetail(id))
                .build();
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public APIResponse<DisputeResponse> resolveDispute(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveDisputeRequest request) {
        return APIResponse.<DisputeResponse>builder()
                .result(disputeService.resolveDispute(id, request))
                .build();
    }
}
