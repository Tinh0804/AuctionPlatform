package com.ecommerce.auctionplatform.controller;

import com.ecommerce.auctionplatform.dto.request.PaymentRequest;
import com.ecommerce.auctionplatform.dto.respose.APIResponse;
import com.ecommerce.auctionplatform.dto.respose.PaymentCallbackResponse;
import com.ecommerce.auctionplatform.dto.respose.PaymentResponse;
import com.ecommerce.auctionplatform.service.MoMoService;
import com.ecommerce.auctionplatform.service.VNPayService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    MoMoService moMoService;
    VNPayService vnPayService;

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
    public ResponseEntity<PaymentCallbackResponse> moMoCallback(@RequestBody Map<String, Object> callbackData) {
        try {
            PaymentCallbackResponse response = moMoService.processCallback(callbackData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/vnpay/create")
    public APIResponse<PaymentResponse> createVNPayPayment(@RequestBody PaymentRequest request) {
        PaymentResponse response = vnPayService.createPayment(request);
        return APIResponse.<PaymentResponse>builder()
                .status(200)
                .message("Payment URL created successfully")
                .result(response)
                .build();
    }

    @GetMapping("/vnpay/callback")
    public ResponseEntity<PaymentCallbackResponse> vnPayCallback(@RequestParam Map<String, String> params) {
        try {
            PaymentCallbackResponse response = vnPayService.processCallback(params);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
