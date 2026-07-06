package com.ecommerce.auctionplatform.controller;

import com.ecommerce.auctionplatform.dto.request.EscrowPaymentRequest;
import com.ecommerce.auctionplatform.dto.request.OrderPaymentRequest;
import com.ecommerce.auctionplatform.dto.request.ShippingUpdateRequest;
import com.ecommerce.auctionplatform.dto.respose.APIResponse;
import com.ecommerce.auctionplatform.dto.respose.OrderPaymentResponse;
import com.ecommerce.auctionplatform.dto.respose.OrderResponse;
import com.ecommerce.auctionplatform.service.OrderService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {

    OrderService orderService;

    @GetMapping("/me/purchases")
    public APIResponse<List<OrderResponse>> getMyPurchases() {
        return APIResponse.<List<OrderResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("My purchases fetched successfully")
                .result(orderService.getMyPurchases())
                .build();
    }

    @GetMapping("/me/sales")
    public APIResponse<List<OrderResponse>> getMySales() {
        return APIResponse.<List<OrderResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("My sales fetched successfully")
                .result(orderService.getMySales())
                .build();
    }

    @GetMapping("/{orderId}")
    public APIResponse<OrderResponse> getOrderDetail(@PathVariable UUID orderId) {
        return APIResponse.<OrderResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Order detail fetched")
                .result(orderService.getOrderDetail(orderId))
                .build();
    }

    @PostMapping("/{orderId}/initiate-payment")
    public APIResponse<OrderPaymentResponse> initiatePayment(
            @PathVariable UUID orderId,
            @RequestBody @Valid OrderPaymentRequest request) {
        return APIResponse.<OrderPaymentResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Payment initiated")
                .result(orderService.initiateOrderPayment(orderId, request))
                .build();
    }

    @PostMapping("/{orderId}/pay")
    public APIResponse<OrderResponse> payOrderWithEscrow(@PathVariable UUID orderId, @RequestBody EscrowPaymentRequest request) {
        OrderResponse response = orderService.payOrderWithEscrow(orderId, request);
        return APIResponse.<OrderResponse>builder()
                .status(200)
                .message("Payment successful. Amount held in Escrow.")
                .result(response)
                .build();
    }

    @PostMapping("/{orderId}/confirm-delivery")
    public APIResponse<OrderResponse> confirmDeliveryAndReleaseEscrow(@PathVariable UUID orderId) {
        OrderResponse response = orderService.confirmDeliveryAndReleaseEscrow(orderId);
        return APIResponse.<OrderResponse>builder()
                .status(200)
                .message("Delivery confirmed. Escrow released to seller.")
                .result(response)
                .build();
    }

    @PostMapping("/{orderId}/shipping")
    public APIResponse<OrderResponse> updateShippingInfo(@PathVariable UUID orderId, @RequestBody ShippingUpdateRequest request) {
        OrderResponse response = orderService.updateShippingInfo(orderId, request);
        return APIResponse.<OrderResponse>builder()
                .status(200)
                .message("Shipping info updated successfully.")
                .result(response)
                .build();
    }
}
