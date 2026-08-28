package com.ecommerce.auctionplatform.payment.application.port.in;

import com.ecommerce.auctionplatform.payment.application.dto.command.SetupPinCommand;
import com.ecommerce.auctionplatform.payment.application.dto.command.RequestWithdrawalCommand;
import com.ecommerce.auctionplatform.payment.application.dto.response.TransactionResponse;

import java.util.List;

/**
 * Port/In – Use case interface for Wallet domain.
 */
public interface WalletUseCase {

    void setupPin(SetupPinCommand request);

    void requestWithdrawal(RequestWithdrawalCommand request);

    List<TransactionResponse> getMyWalletHistory();

}
