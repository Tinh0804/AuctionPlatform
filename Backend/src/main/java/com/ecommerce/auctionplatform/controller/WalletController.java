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
import org.springframework.web.bind.annotation.RestController;

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
}
