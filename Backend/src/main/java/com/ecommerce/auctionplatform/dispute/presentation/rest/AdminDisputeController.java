package com.ecommerce.auctionplatform.dispute.presentation.rest;

import com.ecommerce.auctionplatform.dispute.application.dto.command.ResolveDisputeCommand;
import com.ecommerce.auctionplatform.shared.presentation.response.APIResponse;
import com.ecommerce.auctionplatform.dispute.application.port.in.DisputeUseCase;
import com.ecommerce.auctionplatform.dispute.presentation.dto.request.ResolveDisputeRequest;
import com.ecommerce.auctionplatform.dispute.presentation.dto.response.DisputeResponse;
import com.ecommerce.auctionplatform.dispute.presentation.mapper.DisputeResponseMapper;
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

    private final DisputeUseCase disputeService;
    private final DisputeResponseMapper responseMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public APIResponse<List<DisputeResponse>> getAllDisputes() {
        return APIResponse.<List<DisputeResponse>>builder()
                .result(responseMapper.toResponses(disputeService.getAllDisputes()))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public APIResponse<DisputeResponse> getDisputeDetail(@PathVariable UUID id) {
        return APIResponse.<DisputeResponse>builder()
                .result(responseMapper.toResponse(disputeService.getDisputeDetail(id)))
                .build();
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public APIResponse<DisputeResponse> resolveDispute(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveDisputeRequest request) {
        return APIResponse.<DisputeResponse>builder()
                .result(responseMapper.toResponse(
                        disputeService.resolveDispute(id, ResolveDisputeCommand.builder()
                                .outcome(request.outcome())
                                .resolution(request.resolution())
                                .build())))
                .build();
    }
}
