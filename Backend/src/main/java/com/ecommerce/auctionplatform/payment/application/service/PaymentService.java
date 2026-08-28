package com.ecommerce.auctionplatform.payment.application.service;

import com.ecommerce.auctionplatform.payment.application.dto.command.PaymentCommand;
import com.ecommerce.auctionplatform.payment.application.dto.response.PaymentCallbackResponse;
import com.ecommerce.auctionplatform.payment.application.dto.response.PaymentResponse;
import com.ecommerce.auctionplatform.payment.application.port.in.PaymentUseCase;
import com.ecommerce.auctionplatform.payment.application.port.out.PaymentGatewayPort;
import com.ecommerce.auctionplatform.payment.application.port.out.PaymentGatewayPort.GatewayCallbackResult;
import com.ecommerce.auctionplatform.payment.application.port.out.PaymentGatewayPort.GatewayPaymentRequest;
import com.ecommerce.auctionplatform.payment.application.port.out.PaymentGatewayPort.GatewayPaymentResult;
import com.ecommerce.auctionplatform.payment.domain.enums.PaymentMethod;
import com.ecommerce.auctionplatform.payment.domain.enums.TransactionStatus;
import com.ecommerce.auctionplatform.payment.domain.enums.TransactionType;
import com.ecommerce.auctionplatform.payment.application.event.OrderPaymentSucceededEvent;
import com.ecommerce.auctionplatform.payment.domain.model.Transaction;
import com.ecommerce.auctionplatform.payment.domain.model.Wallet;
import com.ecommerce.auctionplatform.payment.domain.repository.TransactionRepository;
import com.ecommerce.auctionplatform.payment.domain.repository.WalletRepository;
import com.ecommerce.auctionplatform.shared.application.event.DomainEventPublisher;
import com.ecommerce.auctionplatform.shared.application.exception.AppException;
import com.ecommerce.auctionplatform.shared.application.exception.ErrorCode;
import com.ecommerce.auctionplatform.shared.application.port.out.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService implements PaymentUseCase {
    private final List<PaymentGatewayPort> paymentGateways;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUserProvider currentUserProvider;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentCommand command) {
        validate(command);
        PaymentGatewayPort gateway = gateway(command.method());
        Transaction transaction = pendingTransaction(command, gateway.gatewayType());
        GatewayPaymentResult result = gateway.createPayment(new GatewayPaymentRequest(
                transaction.getId(),
                transaction.getAmount(),
                command.orderInfo(),
                command.returnUrl(),
                command.notifyUrl()));

        return PaymentResponse.builder()
                .status("SUCCESS")
                .message(result.message())
                .paymentUrl(result.paymentUrl())
                .orderId(result.externalOrderId())
                .transactionId(transaction.getId().toString())
                .amount(transaction.getAmount().doubleValue())
                .paymentMethod(gateway.gatewayType().name())
                .build();
    }

    @Override
    @Transactional
    public PaymentCallbackResponse processCallback(PaymentMethod method, Map<String, String> callbackData) {
        PaymentGatewayPort gateway = gateway(method);
        GatewayCallbackResult callback = gateway.verifyCallback(callbackData);
        if (!callback.signatureValid() || callback.transactionId() == null) {
            return callbackResponse(gateway.gatewayType(), callback, TransactionStatus.FAILED);
        }

        Transaction transaction = transactionRepository.findById(callback.transactionId())
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            return callbackResponse(gateway.gatewayType(), callback, transaction.getStatus());
        }
        if (callback.amount() == null || callback.amount().compareTo(transaction.getAmount()) != 0) {
            transaction.markAsFailed("Gateway callback amount does not match pending transaction");
            transactionRepository.save(transaction);
            return callbackResponse(gateway.gatewayType(), callback, TransactionStatus.FAILED);
        }

        if (callback.successful()) {
            transaction.markAsSuccess(callback.externalTransactionId(), callback.rawPayload());
            transactionRepository.save(transaction);
            completeSuccessfulPayment(transaction);
        } else {
            transaction.markAsFailed(callback.rawPayload());
            transactionRepository.save(transaction);
        }
        return callbackResponse(
                gateway.gatewayType(),
                callback,
                callback.successful() ? TransactionStatus.SUCCESS : TransactionStatus.FAILED);
    }

    private void completeSuccessfulPayment(Transaction transaction) {
        if ("ORDER".equals(transaction.getReferenceType()) && transaction.getReferenceId() != null) {
            domainEventPublisher.publish(new OrderPaymentSucceededEvent(
                    transaction.getReferenceId(), transaction.getAmount()));
            return;
        }
        Wallet wallet = walletRepository.findByIdForUpdate(transaction.getWalletId())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        wallet.addBalance(transaction.getAmount());
        walletRepository.save(wallet);
    }

    private Transaction pendingTransaction(PaymentCommand command, PaymentMethod method) {
        if (command.referenceId() != null && !command.referenceId().isBlank()) {
            try {
                Transaction existing = transactionRepository.findById(UUID.fromString(command.referenceId()))
                        .orElse(null);
                if (existing != null) {
                    if (existing.getStatus() != TransactionStatus.PENDING
                            || existing.getAmount().compareTo(BigDecimal.valueOf(command.amount())) != 0) {
                        throw new AppException(ErrorCode.BAD_REQUEST);
                    }
                    return existing;
                }
            } catch (IllegalArgumentException ignored) {
                // A caller reference is not necessarily a transaction id; create a new deposit below.
            }
        }

        UUID userId = currentUserProvider.currentProfileId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        return transactionRepository.save(Transaction.builder()
                .walletId(wallet.getId())
                .type(TransactionType.DEPOSIT)
                .amount(BigDecimal.valueOf(command.amount()))
                .status(TransactionStatus.PENDING)
                .gatewayProvider(method.name())
                .note(command.orderInfo())
                .build());
    }

    private PaymentCallbackResponse callbackResponse(
            PaymentMethod method,
            GatewayCallbackResult callback,
            TransactionStatus status
    ) {
        return PaymentCallbackResponse.builder()
                .orderId(callback.transactionId() == null ? null : callback.transactionId().toString())
                .transactionId(callback.externalTransactionId())
                .amount(callback.amount() == null ? null : callback.amount().longValue())
                .paymentStatus(status.name())
                .paymentMethod(method.name())
                .message(callback.message())
                .paymentTime(callback.paymentTime())
                .build();
    }

    private PaymentGatewayPort gateway(PaymentMethod method) {
        return paymentGateways.stream()
                .filter(candidate -> candidate.gatewayType() == method)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED));
    }

    private void validate(PaymentCommand command) {
        if (command == null || command.method() == null || command.amount() == null || command.amount() <= 0) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
    }
}
