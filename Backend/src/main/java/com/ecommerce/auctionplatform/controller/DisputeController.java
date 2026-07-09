package com.ecommerce.auctionplatform.controller;

import com.ecommerce.auctionplatform.dto.request.CreateDisputeRequest;
import com.ecommerce.auctionplatform.dto.respose.APIResponse;
import com.ecommerce.auctionplatform.dto.respose.DisputeResponse;
import com.ecommerce.auctionplatform.service.DisputeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/disputes")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public APIResponse<DisputeResponse> createDispute(
            @Valid @ModelAttribute CreateDisputeRequest request,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {
        return APIResponse.<DisputeResponse>builder()
                .result(disputeService.createDispute(request, files))
                .build();
    }

    @GetMapping("/me")
    public APIResponse<List<DisputeResponse>> getMyDisputes() {
        return APIResponse.<List<DisputeResponse>>builder()
                .result(disputeService.getMyDisputes())
                .build();
    }

    @GetMapping("/{id}")
    public APIResponse<DisputeResponse> getDisputeDetail(@PathVariable UUID id) {
        return APIResponse.<DisputeResponse>builder()
                .result(disputeService.getDisputeDetail(id))
                .build();
    }
}
