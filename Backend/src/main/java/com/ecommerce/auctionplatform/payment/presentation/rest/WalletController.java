package com.ecommerce.auctionplatform.payment.presentation.rest;

import com.ecommerce.auctionplatform.payment.application.dto.command.SetupPinCommand;
import com.ecommerce.auctionplatform.payment.application.dto.command.RequestWithdrawalCommand;
import com.ecommerce.auctionplatform.shared.presentation.response.APIResponse;
import com.ecommerce.auctionplatform.payment.application.port.in.WalletUseCase;
import com.ecommerce.auctionplatform.payment.presentation.dto.response.DepositResponse;
import com.ecommerce.auctionplatform.payment.presentation.dto.response.PaymentResponse;
import com.ecommerce.auctionplatform.payment.presentation.dto.response.TransactionResponse;
import com.ecommerce.auctionplatform.payment.presentation.mapper.PaymentResponseMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import com.ecommerce.auctionplatform.payment.application.dto.command.PaymentCommand;
import com.ecommerce.auctionplatform.payment.application.port.in.PaymentUseCase;
import com.ecommerce.auctionplatform.payment.domain.enums.PaymentMethod;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import com.ecommerce.auctionplatform.payment.presentation.dto.request.WithdrawRequest;
import com.ecommerce.auctionplatform.payment.presentation.dto.request.PinSetupRequest;
import java.util.List;
import java.math.BigDecimal;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WalletController {
    WalletUseCase walletService;
    PaymentUseCase paymentUseCase;
    PaymentResponseMapper responseMapper;

    @PostMapping("/pin/setup")
    public APIResponse<Void> setupPin(@Valid @RequestBody PinSetupRequest request) {
        walletService.setupPin(SetupPinCommand.builder()
                .firebaseIdToken(request.firebaseIdToken())
                .newPin(request.newPin())
                .build());
        return APIResponse.<Void>builder()
                .status(200)
                .message("PIN setup completed successfully")
                .build();
    }


    @PostMapping("/deposit/request")
    public APIResponse<DepositResponse> requestDeposit(@RequestParam Long amount, @RequestParam String provider) {
        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return APIResponse.<DepositResponse>builder()
                    .status(200)
                    .message("Deposit request created successfully")
                    .result(new DepositResponse(null, "Payment method not avalible"))
                    .build();
        }

        PaymentCommand command = PaymentCommand.builder()
                .amount(amount.doubleValue())
                .orderInfo("Nạp tiền vào ví")
                .method(method)
                .referenceId(UUID.randomUUID().toString())
                .build();
        PaymentResponse payRes = responseMapper.toPaymentResponse(paymentUseCase.createPayment(command));

        return APIResponse.<DepositResponse>builder()
                .status(200)
                .message("Deposit request created successfully")
                .result(new DepositResponse(payRes.paymentUrl(), null))
                .build();
    }

    @PostMapping("/withdraw")
    public APIResponse<Void> requestWithdraw(@Valid @RequestBody WithdrawRequest request) {
        walletService.requestWithdrawal(new RequestWithdrawalCommand(
                request.getBank(),
                request.getAccount_number(),
                BigDecimal.valueOf(request.getAmount())));
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
                .result(responseMapper.toTransactionResponses(walletService.getMyWalletHistory()))
                .build();
    }
}
