package com.ecommerce.auctionplatform.payment.application.service;

import com.ecommerce.auctionplatform.payment.application.port.out.PaymentGatewayPort;
import com.ecommerce.auctionplatform.payment.domain.enums.PaymentMethod;
import com.ecommerce.auctionplatform.payment.domain.enums.TransactionStatus;
import com.ecommerce.auctionplatform.payment.domain.enums.TransactionType;
import com.ecommerce.auctionplatform.payment.domain.model.Transaction;
import com.ecommerce.auctionplatform.payment.domain.model.Wallet;
import com.ecommerce.auctionplatform.payment.domain.repository.TransactionRepository;
import com.ecommerce.auctionplatform.payment.domain.repository.WalletRepository;
import com.ecommerce.auctionplatform.shared.application.event.DomainEventPublisher;
import com.ecommerce.auctionplatform.shared.application.port.out.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    private PaymentGatewayPort gateway;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private DomainEventPublisher eventPublisher;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        when(gateway.gatewayType()).thenReturn(PaymentMethod.MOMO);
        paymentService = new PaymentService(
                List.of(gateway), walletRepository, transactionRepository, currentUserProvider, eventPublisher);
    }

    @Test
    void successfulDepositCallbackIsCompletedByApplicationNotGateway() {
        UUID transactionId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        Transaction transaction = Transaction.builder()
                .id(transactionId)
                .walletId(walletId)
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("100000"))
                .status(TransactionStatus.PENDING)
                .build();
        Wallet wallet = Wallet.builder()
                .id(walletId)
                .userId(UUID.randomUUID())
                .availableBalance(BigDecimal.ZERO)
                .frozenBalance(BigDecimal.ZERO)
                .build();
        when(gateway.verifyCallback(Map.of("orderId", transactionId.toString())))
                .thenReturn(callback(transactionId, new BigDecimal("100000"), true, true));
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(walletRepository.findByIdForUpdate(walletId)).thenReturn(Optional.of(wallet));

        var response = paymentService.processCallback(
                PaymentMethod.MOMO, Map.of("orderId", transactionId.toString()));

        assertEquals("SUCCESS", response.getPaymentStatus());
        assertEquals(TransactionStatus.SUCCESS, transaction.getStatus());
        assertEquals(new BigDecimal("100000"), wallet.getAvailableBalance());
        verify(transactionRepository).save(transaction);
        verify(walletRepository).save(wallet);
    }

    @Test
    void mismatchedCallbackAmountFailsWithoutCreditingWallet() {
        UUID transactionId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        Transaction transaction = Transaction.builder()
                .id(transactionId)
                .walletId(walletId)
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("100000"))
                .status(TransactionStatus.PENDING)
                .build();
        when(gateway.verifyCallback(Map.of()))
                .thenReturn(callback(transactionId, new BigDecimal("99999"), true, true));
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        var response = paymentService.processCallback(PaymentMethod.MOMO, Map.of());

        assertEquals("FAILED", response.getPaymentStatus());
        assertEquals(TransactionStatus.FAILED, transaction.getStatus());
        verify(walletRepository, never()).findById(walletId);
        verify(walletRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private PaymentGatewayPort.GatewayCallbackResult callback(
            UUID transactionId,
            BigDecimal amount,
            boolean signatureValid,
            boolean successful
    ) {
        return new PaymentGatewayPort.GatewayCallbackResult(
                transactionId,
                "external-transaction",
                amount,
                signatureValid,
                successful,
                "ok",
                "now",
                "raw");
    }
}
