package com.ecommerce.auctionplatform.payment.infrastructure.messaging;

import com.ecommerce.auctionplatform.auction.application.event.AuctionEndedEvent;
import com.ecommerce.auctionplatform.auction.application.event.WinnerPromotedEvent;
import com.ecommerce.auctionplatform.payment.application.port.in.OrderCreationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {
    private final OrderCreationUseCase orderCreationUseCase;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAuctionEnded(AuctionEndedEvent event) {
        log.info("Creating order for winner {} in auction {}", event.winnerId(), event.auctionId());
        orderCreationUseCase.createForWinner(event.auctionId(), event.winnerId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWinnerPromoted(WinnerPromotedEvent event) {
        log.info("Creating order for promoted winner {} in auction {}", event.newWinnerId(), event.auctionId());
        orderCreationUseCase.createForWinner(event.auctionId(), event.newWinnerId());
    }
}
