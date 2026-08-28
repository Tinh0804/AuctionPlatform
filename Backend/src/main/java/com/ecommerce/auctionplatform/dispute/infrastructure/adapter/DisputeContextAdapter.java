package com.ecommerce.auctionplatform.dispute.infrastructure.adapter;

import com.ecommerce.auctionplatform.dispute.application.port.out.DisputeContextPort;
import com.ecommerce.auctionplatform.dispute.application.port.out.DisputeOrderView;
import com.ecommerce.auctionplatform.payment.application.port.in.PaymentDisputeUseCase;
import com.ecommerce.auctionplatform.identity.application.port.in.ReputationUseCase;
import com.ecommerce.auctionplatform.identity.application.port.in.UserDirectoryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/** Thin anti-corruption adapter between dispute, payment and user contracts. */
@Component
@RequiredArgsConstructor
public class DisputeContextAdapter implements DisputeContextPort {
    private final PaymentDisputeUseCase paymentDisputeUseCase;
    private final UserDirectoryUseCase userDirectoryUseCase;
    private final ReputationUseCase reputationUseCase;

    @Override
    public Optional<DisputeOrderView> findOrder(UUID orderId) {
        return paymentDisputeUseCase.findOrder(orderId).map(this::toView);
    }

    @Override
    public Optional<String> findUserName(UUID userId) {
        return userDirectoryUseCase.findById(userId).map(UserDirectoryUseCase.UserProfile::name);
    }

    @Override
    public Optional<UUID> findAdminId() {
        return userDirectoryUseCase.findAdmin().map(UserDirectoryUseCase.UserProfile::id);
    }

    @Override
    public void markOrderDisputed(UUID orderId) {
        paymentDisputeUseCase.markOrderDisputed(orderId);
    }

    @Override
    public void settleBuyerWin(DisputeOrderView order, UUID disputeId) {
        paymentDisputeUseCase.settleBuyerWin(order.id(), disputeId);
    }

    @Override
    public void settleSellerWin(DisputeOrderView order, UUID disputeId, UUID adminId) {
        paymentDisputeUseCase.settleSellerWin(order.id(), disputeId, adminId);
    }

    @Override
    public void decreaseReputation(UUID userId, int points, String reason, UUID disputeId) {
        reputationUseCase.decreaseForDispute(userId, points, reason, disputeId);
    }

    private DisputeOrderView toView(PaymentDisputeUseCase.DisputeOrderSnapshot order) {
        return new DisputeOrderView(
                order.id(), order.auctionRecordId(), order.buyerId(), order.sellerId(),
                order.amount(), order.eligibleForDispute(), order.trackingCode(), order.updatedAt(),
                order.productName(), order.productImageUrl());
    }
}
