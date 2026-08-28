package com.ecommerce.auctionplatform.dispute.presentation.rest;

import com.ecommerce.auctionplatform.dispute.application.dto.command.CreateDisputeCommand;
import com.ecommerce.auctionplatform.shared.presentation.response.APIResponse;
import com.ecommerce.auctionplatform.dispute.application.port.in.DisputeUseCase;
import com.ecommerce.auctionplatform.shared.presentation.mapper.FileUploadMapper;
import com.ecommerce.auctionplatform.dispute.presentation.dto.request.CreateDisputeRequest;
import com.ecommerce.auctionplatform.dispute.presentation.dto.response.DisputeResponse;
import com.ecommerce.auctionplatform.dispute.presentation.mapper.DisputeResponseMapper;
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

    private final DisputeUseCase disputeService;
    private final DisputeResponseMapper responseMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public APIResponse<DisputeResponse> createDispute(
            @Valid @ModelAttribute CreateDisputeRequest request,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {
        return APIResponse.<DisputeResponse>builder()
                .result(responseMapper.toResponse(
                        disputeService.createDispute(CreateDisputeCommand.builder()
                                        .orderId(request.orderId())
                                        .reason(request.reason())
                                        .description(request.description())
                                        .build(),
                                FileUploadMapper.toContents(files))))
                .build();
    }

    @GetMapping("/me")
    public APIResponse<List<DisputeResponse>> getMyDisputes() {
        return APIResponse.<List<DisputeResponse>>builder()
                .result(responseMapper.toResponses(disputeService.getMyDisputes()))
                .build();
    }

    @GetMapping("/{id}")
    public APIResponse<DisputeResponse> getDisputeDetail(@PathVariable UUID id) {
        return APIResponse.<DisputeResponse>builder()
                .result(responseMapper.toResponse(disputeService.getDisputeDetail(id)))
                .build();
    }
}
