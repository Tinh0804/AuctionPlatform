package com.ecommerce.auctionplatform.payment.infrastructure.messaging;

import com.ecommerce.auctionplatform.auction.application.event.AuctionEndedEvent;
import com.ecommerce.auctionplatform.auction.application.event.DepositForfeitedEvent;
import com.ecommerce.auctionplatform.auction.application.event.DepositReservationRequestedEvent;
import com.ecommerce.auctionplatform.payment.application.port.in.AuctionPaymentEventUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {
    private final AuctionPaymentEventUseCase paymentEventUseCase;

    @EventListener
    public void onDepositReservationRequested(DepositReservationRequestedEvent event) {
        log.info("Processing deposit reservation for user {} in auction {}", event.userId(), event.auctionId());
        paymentEventUseCase.reserveDeposit(event.userId(), event.auctionId(), event.amount());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDepositForfeited(DepositForfeitedEvent event) {
        log.info("Processing forfeit deposit for user {} in auction {}", event.userId(), event.auctionId());
        paymentEventUseCase.forfeitDeposit(event.userId(), event.auctionId(), event.depositAmount());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAuctionEnded(AuctionEndedEvent event) {
        log.info("Refunding deposits for losers of auction {}", event.auctionId());
        paymentEventUseCase.refundLoserDeposits(event.auctionId(), event.winnerId());
    }
}
