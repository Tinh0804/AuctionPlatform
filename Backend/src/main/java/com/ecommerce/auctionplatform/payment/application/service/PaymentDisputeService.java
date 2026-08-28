package com.ecommerce.auctionplatform.payment.application.service;

import com.ecommerce.auctionplatform.payment.application.port.in.PaymentDisputeUseCase;
import com.ecommerce.auctionplatform.payment.application.port.out.AuctionQueryPort;
import com.ecommerce.auctionplatform.payment.application.port.out.AuctionRecordView;
import com.ecommerce.auctionplatform.payment.domain.enums.OrderStatus;
import com.ecommerce.auctionplatform.payment.domain.enums.TransactionStatus;
import com.ecommerce.auctionplatform.payment.domain.enums.TransactionType;
import com.ecommerce.auctionplatform.payment.domain.enums.WalletStatus;
import com.ecommerce.auctionplatform.payment.domain.model.Order;
import com.ecommerce.auctionplatform.payment.domain.model.Transaction;
import com.ecommerce.auctionplatform.payment.domain.model.Wallet;
import com.ecommerce.auctionplatform.payment.domain.repository.OrderRepository;
import com.ecommerce.auctionplatform.payment.domain.repository.TransactionRepository;
import com.ecommerce.auctionplatform.payment.domain.repository.WalletRepository;
import com.ecommerce.auctionplatform.shared.application.exception.AppException;
import com.ecommerce.auctionplatform.shared.application.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentDisputeService implements PaymentDisputeUseCase {
    private static final BigDecimal FALLBACK_PLATFORM_FEE_RATE = new BigDecimal("0.05");

    private final OrderRepository orderRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final AuctionQueryPort auctionQueryPort;

    @Override
    @Transactional(readOnly = true)
    public Optional<DisputeOrderSnapshot> findOrder(UUID orderId) {
        return orderRepository.findById(orderId).map(this::toSnapshot);
    }

    @Override
    @Transactional
    public void markOrderDisputed(UUID orderId) {
        Order order = getOrder(orderId);
        order.markAsDisputed();
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void settleBuyerWin(UUID orderId, UUID disputeId) {
        Order order = getOrder(orderId);
        Wallet sellerWallet = getWallet(order.getSellerId());
        Wallet buyerWallet = getWallet(order.getBuyerId());
        sellerWallet.deductFrozenBalance(order.getTotalAmount());
        buyerWallet.addBalance(order.getTotalAmount());
        walletRepository.save(sellerWallet);
        walletRepository.save(buyerWallet);
        saveTransaction(buyerWallet, order.getTotalAmount(), TransactionType.DISPUTE_REFUND,
                "Hoàn tiền do thắng khiếu nại sản phẩm: " + productName(order), disputeId);
        order.cancel();
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void settleSellerWin(UUID orderId, UUID disputeId, UUID adminId) {
        Order order = getOrder(orderId);
        Wallet sellerWallet = getWallet(order.getSellerId());
        sellerWallet.deductFrozenBalance(order.getTotalAmount());

        BigDecimal platformFee = platformFee(order);
        BigDecimal sellerReceived = order.getTotalAmount().subtract(platformFee);
        sellerWallet.addBalance(sellerReceived);
        walletRepository.save(sellerWallet);
        saveTransaction(sellerWallet, sellerReceived, TransactionType.ESCROW_RELEASE,
                "Giải ngân (Thắng khiếu nại) sản phẩm: " + productName(order), disputeId);

        Wallet adminWallet = walletRepository.findByUserId(adminId).orElseGet(() -> walletRepository.save(Wallet.builder()
                .userId(adminId)
                .availableBalance(BigDecimal.ZERO)
                .frozenBalance(BigDecimal.ZERO)
                .status(WalletStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build()));
        if (platformFee.signum() > 0) {
            adminWallet.addBalance(platformFee);
            walletRepository.save(adminWallet);
            saveTransaction(adminWallet, platformFee, TransactionType.PLATFORM_FEE,
                    "Phí nền tảng từ sản phẩm: " + productName(order), null);
        }

        order.markAsCompleted();
        orderRepository.save(order);
    }

    private DisputeOrderSnapshot toSnapshot(Order order) {
        AuctionRecordView record = auctionRecord(order).orElse(null);
        return new DisputeOrderSnapshot(
                order.getId(), order.getAuctionRecordId(), order.getBuyerId(), order.getSellerId(),
                order.getTotalAmount(),
                order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.SHIPPING,
                order.getTrackingCode(),
                order.getUpdatedAt() == null ? order.getCreatedAt() : order.getUpdatedAt(),
                record == null ? null : record.productName(),
                record == null ? null : record.productImageUrl());
    }

    private Order getOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
    }

    private Wallet getWallet(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
    }

    private Optional<AuctionRecordView> auctionRecord(Order order) {
        return order.getAuctionRecordId() == null
                ? Optional.empty()
                : auctionQueryPort.findRecord(order.getAuctionRecordId());
    }

    private String productName(Order order) {
        return auctionRecord(order).map(AuctionRecordView::productName).orElse("Unknown");
    }

    private BigDecimal platformFee(Order order) {
        return auctionRecord(order)
                .map(AuctionRecordView::platformFee)
                .filter(fee -> fee.signum() >= 0 && fee.compareTo(order.getTotalAmount()) <= 0)
                .orElseGet(() -> order.getTotalAmount().multiply(FALLBACK_PLATFORM_FEE_RATE));
    }

    private void saveTransaction(Wallet wallet, BigDecimal amount, TransactionType type, String note, UUID disputeId) {
        transactionRepository.save(Transaction.builder()
                .walletId(wallet.getId())
                .amount(amount)
                .type(type)
                .status(TransactionStatus.SUCCESS)
                .note(note)
                .referenceType(disputeId == null ? null : "DISPUTE")
                .referenceId(disputeId)
                .build());
    }
}
