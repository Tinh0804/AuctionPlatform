package com.ecommerce.auctionplatform.controller;

import com.ecommerce.auctionplatform.dto.request.EscrowPaymentRequest;
import com.ecommerce.auctionplatform.dto.respose.APIResponse;
import com.ecommerce.auctionplatform.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {

    OrderService orderService;

    @PostMapping("/{orderId}/pay")
    public APIResponse<Void> payOrderWithEscrow(@PathVariable UUID orderId, @RequestBody EscrowPaymentRequest request) {
        orderService.payOrderWithEscrow(orderId, request);
        return APIResponse.<Void>builder()
                .status(200)
                .message("Payment successful. Amount held in Escrow.")
                .build();
    }

    @PostMapping("/{orderId}/confirm-delivery")
    public APIResponse<Void> confirmDeliveryAndReleaseEscrow(@PathVariable UUID orderId) {
        orderService.confirmDeliveryAndReleaseEscrow(orderId);
        return APIResponse.<Void>builder()
                .status(200)
                .message("Delivery confirmed. Escrow released to seller.")
                .build();
    }
}
