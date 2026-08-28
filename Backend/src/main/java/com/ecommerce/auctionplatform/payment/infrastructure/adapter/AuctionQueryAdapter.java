package com.ecommerce.auctionplatform.payment.infrastructure.adapter;

import com.ecommerce.auctionplatform.auction.application.port.in.AuctionSettlementUseCase;
import com.ecommerce.auctionplatform.payment.application.port.out.AuctionQueryPort;
import com.ecommerce.auctionplatform.payment.application.port.out.AuctionRecordView;
import com.ecommerce.auctionplatform.payment.application.port.out.AuctionRegistrationView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuctionQueryAdapter implements AuctionQueryPort {
    private final AuctionSettlementUseCase auctionSettlementUseCase;

    @Override
    public Optional<AuctionRecordView> findRecord(UUID auctionRecordId) {
        return auctionSettlementUseCase.findRecord(auctionRecordId).map(this::toView);
    }

    @Override
    public Optional<AuctionRecordView> findRecord(UUID auctionId, UUID userId) {
        return auctionSettlementUseCase.findRecord(auctionId, userId).map(this::toView);
    }

    @Override
    public List<AuctionRegistrationView> findRegistrations(UUID auctionId) {
        return auctionSettlementUseCase.findRegistrations(auctionId).stream()
                .map(this::toView)
                .toList();
    }

    @Override
    public Optional<AuctionRegistrationView> findRegistration(UUID auctionId, UUID userId) {
        return auctionSettlementUseCase.findRegistration(auctionId, userId).map(this::toView);
    }

    @Override
    public void markRegistrationRefunded(UUID registrationId) {
        auctionSettlementUseCase.markRegistrationRefunded(registrationId);
    }

    @Override
    public void markRecordWon(UUID auctionRecordId) {
        auctionSettlementUseCase.markRecordWon(auctionRecordId);
    }

    private AuctionRecordView toView(AuctionSettlementUseCase.AuctionRecordSummary record) {
        return new AuctionRecordView(
                record.id(), record.auctionId(), record.sellerId(), record.productId(),
                record.productName(), record.productImageUrl(), record.depositAmount(),
                record.platformFee(), record.finalPrice(), record.paymentDeadline()
        );
    }

    private AuctionRegistrationView toView(AuctionSettlementUseCase.AuctionRegistrationSummary registration) {
        return new AuctionRegistrationView(
                registration.id(), registration.auctionId(), registration.userId(),
                registration.depositAmount(), registration.paid()
        );
    }
}
