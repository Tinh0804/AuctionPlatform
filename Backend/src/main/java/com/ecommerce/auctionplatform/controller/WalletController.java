package com.ecommerce.auctionplatform.controller;

import com.ecommerce.auctionplatform.dto.request.PinSetupRequest;
import com.ecommerce.auctionplatform.dto.respose.APIResponse;
import com.ecommerce.auctionplatform.service.WalletService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ecommerce.auctionplatform.dto.request.WithdrawRequest;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WalletController {
    WalletService walletService;

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
        response.put("payment_url", "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
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
}
