package com.ecommerce.auctionplatform.controller;

import com.ecommerce.auctionplatform.dto.request.PinSetupRequest;
import com.ecommerce.auctionplatform.dto.respose.APIResponse;
import com.ecommerce.auctionplatform.service.WalletService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import com.ecommerce.auctionplatform.dto.request.PaymentRequest;
import com.ecommerce.auctionplatform.dto.respose.PaymentResponse;
import com.ecommerce.auctionplatform.entity.enums.PaymentMethod;
import com.ecommerce.auctionplatform.service.MoMoService;
import com.ecommerce.auctionplatform.service.VNPayService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import com.ecommerce.auctionplatform.dto.request.WithdrawRequest;
import com.ecommerce.auctionplatform.dto.respose.TransactionResponse;
import java.util.List;

import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WalletController {
    WalletService walletService;
    MoMoService moMoService;
    VNPayService vnPayService;

    @PostMapping("/pin/setup")
    public APIResponse<Void> setupPin(@RequestBody PinSetupRequest request) {
        walletService.setupPin(request);
        return APIResponse.<Void>builder()
                .status(200)
                .message("PIN setup completed successfully")
                .build();
    }


    @PostMapping("/deposit/request")
    public APIResponse<Map<String, String>> requestDeposit(@RequestParam Long amount, @RequestParam String provider) {
        Map<String, String> response = new HashMap<>();
        if (PaymentMethod.MOMO.name().equalsIgnoreCase(provider)) {
            PaymentRequest req = PaymentRequest.builder()
                    .amount(amount.doubleValue())
                    .orderInfo("Nạp tiền vào ví")
                    .method(PaymentMethod.MOMO)
                    .referenceId(UUID.randomUUID().toString())
                    .build();
            PaymentResponse payRes = moMoService.createPayment(req);
            response.put("payment_url", payRes.getPaymentUrl());
        } else if (PaymentMethod.VNPAY.name().equalsIgnoreCase(provider)) {
            PaymentRequest req = PaymentRequest.builder()
                    .amount(amount.doubleValue())
                    .orderInfo("Nạp tiền vào ví")
                    .method(PaymentMethod.VNPAY)
                    .referenceId(UUID.randomUUID().toString())
                    .build();
            PaymentResponse payRes = vnPayService.createPayment(req);
            response.put("payment_url", payRes.getPaymentUrl());
        } else {
            response.put("payment_url", null);
            response.put("message","Payment method not avalible");
        }
        
        return APIResponse.<Map<String, String>>builder()
                .status(200)
                .message("Deposit request created successfully")
                .result(response)
                .build();
    }

    @PostMapping("/withdraw")
    public APIResponse<Void> requestWithdraw(@RequestBody WithdrawRequest request) {
        // Dummy implementation
        return APIResponse.<Void>builder()
                .status(200)
                .message("Withdraw request submitted successfully")
                .build();
    }

    @GetMapping("/history")
    public APIResponse<List<TransactionResponse>> getWalletHistory() {
        return APIResponse.<List<TransactionResponse>>builder()
                .status(200)
                .message("Lấy lịch sử giao dịch thành công")
                .result(walletService.getMyWalletHistory())
                .build();
    }
}
