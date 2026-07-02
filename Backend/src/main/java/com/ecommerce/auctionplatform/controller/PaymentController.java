package com.ecommerce.auctionplatform.controller;

import com.ecommerce.auctionplatform.dto.request.PaymentRequest;
import com.ecommerce.auctionplatform.dto.respose.APIResponse;
import com.ecommerce.auctionplatform.dto.respose.PaymentResponse;
import com.ecommerce.auctionplatform.service.MoMoService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    MoMoService moMoService;

    @PostMapping("/momo/create")
    public APIResponse<PaymentResponse> createMoMoPayment(@RequestBody PaymentRequest request) {
        PaymentResponse response = moMoService.createPayment(request);
        return APIResponse.<PaymentResponse>builder()
                .status(200)
                .message("Payment URL created successfully")
                .result(response)
                .build();
    }

    @PostMapping("/momo/callback")
    public ResponseEntity<Void> moMoCallback(@RequestBody Map<String, Object> callbackData) {
        try {
            moMoService.processCallback(callbackData);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
