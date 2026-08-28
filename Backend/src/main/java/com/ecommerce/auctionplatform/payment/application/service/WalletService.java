package com.ecommerce.auctionplatform.payment.application.service;

import com.ecommerce.auctionplatform.payment.application.dto.command.SetupPinCommand;
import com.ecommerce.auctionplatform.payment.application.dto.command.RequestWithdrawalCommand;
import com.ecommerce.auctionplatform.payment.domain.enums.TransactionStatus;
import com.ecommerce.auctionplatform.payment.domain.enums.TransactionType;
import com.ecommerce.auctionplatform.payment.domain.model.Wallet;
import com.ecommerce.auctionplatform.shared.application.exception.AppException;
import com.ecommerce.auctionplatform.shared.application.exception.ErrorCode;
import com.ecommerce.auctionplatform.payment.domain.enums.WalletStatus;
import com.ecommerce.auctionplatform.payment.domain.repository.TransactionRepository;
import com.ecommerce.auctionplatform.payment.application.port.out.UserPort;
import com.ecommerce.auctionplatform.payment.application.port.out.UserView;
import com.ecommerce.auctionplatform.payment.domain.repository.WalletRepository;
import com.ecommerce.auctionplatform.shared.application.port.out.CurrentUserProvider;
import com.ecommerce.auctionplatform.shared.application.port.out.PasswordCodec;
import com.ecommerce.auctionplatform.shared.application.port.out.PhoneVerificationPort;
import com.ecommerce.auctionplatform.payment.application.dto.response.TransactionResponse;
import com.ecommerce.auctionplatform.payment.domain.model.Transaction;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import com.ecommerce.auctionplatform.payment.application.port.in.WalletUseCase;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WalletService implements WalletUseCase {
    WalletRepository walletRepository;
    UserPort userPort;
    PasswordCodec passwordCodec;
    TransactionRepository transactionRepository;
    CurrentUserProvider currentUserProvider;
    PhoneVerificationPort phoneVerificationPort;

    @Transactional
    public void setupPin(SetupPinCommand request) {
        UUID userProfileId = currentUserProvider.currentProfileId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        UserView user = userPort.findById(userProfileId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String firebasePhone = phoneVerificationPort.verifiedPhoneNumber(request.getFirebaseIdToken())
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST));

        String userPhone = user.phone();
        if (!normalizePhone(firebasePhone).equals(normalizePhone(userPhone))) {
            log.error("Phone number mismatch: Firebase phone = {}, Registered user phone = {}", firebasePhone, userPhone);
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        Wallet wallet = walletRepository.findByUserId(user.id()).orElseGet(() -> {
            Wallet newWallet = Wallet.builder()
                    .userId(user.id())
                    .availableBalance(BigDecimal.ZERO)
                    .frozenBalance(BigDecimal.ZERO)
                    .status(WalletStatus.ACTIVE)
                    .build();
            return walletRepository.save(newWallet);
        });

        wallet.setupPin(passwordCodec.encode(request.getNewPin()));
        walletRepository.save(wallet);

        log.info("Wallet PIN successfully configured for user {}", userProfileId);
    }

    @Override
    @Transactional
    public void requestWithdrawal(RequestWithdrawalCommand request) {
        if (request == null || request.amount() == null || request.amount().signum() <= 0
                || request.bank() == null || request.bank().isBlank()
                || request.accountNumber() == null || request.accountNumber().isBlank()) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        UserView user = getCurrentUser();
        Wallet wallet = walletRepository.findByUserIdForUpdate(user.id())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        wallet.freezeBalance(request.amount());
        walletRepository.save(wallet);
        transactionRepository.save(Transaction.builder()
                .walletId(wallet.getId())
                .type(TransactionType.WITHDRAWAL)
                .amount(request.amount())
                .status(TransactionStatus.PENDING)
                .referenceType("WITHDRAWAL")
                .note("Rút tiền về " + request.bank() + " - " + maskAccount(request.accountNumber()))
                .build());
    }

    public List<TransactionResponse> getMyWalletHistory() {
        UserView user = getCurrentUser();
        Wallet wallet = walletRepository.findByUserId(user.id())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId())
                .stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse mapToTransactionResponse(Transaction tx) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .type(tx.getType())
                .amount(tx.getAmount())
                .status(tx.getStatus())
                .note(tx.getNote())
                .createdAt(tx.getCreatedAt())
                .build();
    }

    private UserView getCurrentUser() {
        UUID userProfileId = currentUserProvider.currentProfileId().orElseThrow(() ->
                new AppException(ErrorCode.UNAUTHORIZED));
        return userPort.findById(userProfileId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private String normalizePhone(String phone) {
        if (phone == null) return "";
        String digits = phone.replaceAll("\\D", ""); // keep only digits
        if (digits.length() >= 9) {
            return digits.substring(digits.length() - 9);
        }
        return digits;
    }

    private String maskAccount(String accountNumber) {
        String normalized = accountNumber.replaceAll("\\s", "");
        if (normalized.length() <= 4) {
            return normalized;
        }
        return "*".repeat(normalized.length() - 4) + normalized.substring(normalized.length() - 4);
    }
}
