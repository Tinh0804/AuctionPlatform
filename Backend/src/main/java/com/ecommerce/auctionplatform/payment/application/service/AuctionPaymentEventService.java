package com.ecommerce.auctionplatform.payment.application.service;

import com.ecommerce.auctionplatform.payment.application.port.in.AuctionPaymentEventUseCase;
import com.ecommerce.auctionplatform.payment.application.port.out.AuctionQueryPort;
import com.ecommerce.auctionplatform.payment.application.port.out.AuctionRegistrationView;
import com.ecommerce.auctionplatform.payment.domain.enums.TransactionStatus;
import com.ecommerce.auctionplatform.payment.domain.enums.TransactionType;
import com.ecommerce.auctionplatform.payment.domain.model.Transaction;
import com.ecommerce.auctionplatform.payment.domain.model.Wallet;
import com.ecommerce.auctionplatform.payment.domain.repository.TransactionRepository;
import com.ecommerce.auctionplatform.payment.domain.repository.WalletRepository;
import com.ecommerce.auctionplatform.shared.application.exception.AppException;
import com.ecommerce.auctionplatform.shared.application.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuctionPaymentEventService implements AuctionPaymentEventUseCase {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final AuctionQueryPort auctionQueryPort;

    @Override
    @Transactional
    public void reserveDeposit(UUID userId, UUID auctionId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        wallet.freezeBalance(amount);
        walletRepository.save(wallet);
        saveTransaction(wallet.getId(), TransactionType.AUCTION_DEPOSIT, amount,
                "AUCTION", auctionId, "Tạm giữ cọc cho đấu giá");
    }

    @Override
    @Transactional
    public void forfeitDeposit(UUID userId, UUID auctionId, BigDecimal amount) {
        walletRepository.findByUserIdForUpdate(userId).ifPresent(wallet -> {
            wallet.deductFrozenBalance(amount);
            walletRepository.save(wallet);
            saveTransaction(wallet.getId(), TransactionType.AUCTION_DEPOSIT_FORFEIT, amount,
                    "AUCTION", auctionId, "Tịch thu cọc do vi phạm đấu giá");
        });
    }

    @Override
    @Transactional
    public void refundLoserDeposits(UUID auctionId, UUID winnerId) {
        for (AuctionRegistrationView registration : auctionQueryPort.findRegistrations(auctionId)) {
            if (registration.userId().equals(winnerId) || !registration.paid()) {
                continue;
            }
            walletRepository.findByUserIdForUpdate(registration.userId()).ifPresent(wallet -> {
                wallet.unfreezeBalance(registration.depositAmount());
                walletRepository.save(wallet);
                auctionQueryPort.markRegistrationRefunded(registration.id());
                saveTransaction(wallet.getId(), TransactionType.AUCTION_DEPOSIT_REFUND,
                        registration.depositAmount(), "REGISTRATION", registration.id(),
                        "Hoàn cọc kết thúc đấu giá");
            });
        }
    }

    private void saveTransaction(
            UUID walletId,
            TransactionType type,
            BigDecimal amount,
            String referenceType,
            UUID referenceId,
            String note
    ) {
        transactionRepository.save(Transaction.builder()
                .walletId(walletId)
                .type(type)
                .amount(amount)
                .status(TransactionStatus.SUCCESS)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .note(note)
                .build());
    }
}
