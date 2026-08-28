package com.ecommerce.auctionplatform.payment.presentation.rest;

import com.ecommerce.auctionplatform.payment.presentation.dto.request.PaymentRequest;
import com.ecommerce.auctionplatform.payment.presentation.dto.response.PaymentCallbackResponse;
import com.ecommerce.auctionplatform.payment.presentation.dto.response.PaymentResponse;
import com.ecommerce.auctionplatform.payment.presentation.mapper.PaymentResponseMapper;
import com.ecommerce.auctionplatform.shared.presentation.response.APIResponse;
import com.ecommerce.auctionplatform.payment.application.dto.command.PaymentCommand;
import com.ecommerce.auctionplatform.payment.application.port.in.PaymentUseCase;
import com.ecommerce.auctionplatform.payment.domain.enums.PaymentMethod;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    PaymentUseCase paymentUseCase;
    PaymentResponseMapper responseMapper;

    @PostMapping("/momo/create")
    public APIResponse<PaymentResponse> createMoMoPayment(@RequestBody PaymentRequest request) {
        PaymentResponse response = responseMapper.toPaymentResponse(
                paymentUseCase.createPayment(toCommand(request, PaymentMethod.MOMO)));
        return APIResponse.<PaymentResponse>builder()
                .status(200)
                .message("Payment URL created successfully")
                .result(response)
                .build();
    }

    @PostMapping("/momo/callback")
    public ResponseEntity<PaymentCallbackResponse> moMoCallback(@RequestBody Map<String, Object> callbackData) {
        Map<String, String> normalized = callbackData.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> String.valueOf(entry.getValue())));
        PaymentCallbackResponse response = responseMapper.toCallbackResponse(
                paymentUseCase.processCallback(PaymentMethod.MOMO, normalized));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/vnpay/create")
    public APIResponse<PaymentResponse> createVNPayPayment(@RequestBody PaymentRequest request) {
        PaymentResponse response = responseMapper.toPaymentResponse(
                paymentUseCase.createPayment(toCommand(request, PaymentMethod.VNPAY)));
        return APIResponse.<PaymentResponse>builder()
                .status(200)
                .message("Payment URL created successfully")
                .result(response)
                .build();
    }

    @GetMapping("/vnpay/callback")
    public ResponseEntity<PaymentCallbackResponse> vnPayCallback(@RequestParam Map<String, String> params) {
        PaymentCallbackResponse response = responseMapper.toCallbackResponse(
                paymentUseCase.processCallback(PaymentMethod.VNPAY, params));
        return ResponseEntity.ok(response);
    }

    private PaymentCommand toCommand(PaymentRequest request, PaymentMethod method) {
        return PaymentCommand.builder()
                .referenceId(request.getReferenceId())
                .amount(request.getAmount())
                .orderInfo(request.getOrderInfo())
                .method(method)
                .returnUrl(request.getReturnUrl())
                .notifyUrl(request.getNotifyUrl())
                .build();
    }
}
