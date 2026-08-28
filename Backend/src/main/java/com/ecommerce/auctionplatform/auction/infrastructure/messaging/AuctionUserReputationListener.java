package com.ecommerce.auctionplatform.auction.infrastructure.messaging;

import com.ecommerce.auctionplatform.auction.application.event.DepositForfeitedEvent;
import com.ecommerce.auctionplatform.identity.application.port.in.ReputationUseCase;
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
public class AuctionUserReputationListener {
    private static final int DEDUCT_REPUTATION_SCORE = 20;

    private final ReputationUseCase reputationUseCase;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDepositForfeited(DepositForfeitedEvent event) {
        log.info("Deducting reputation for user {} after auction {} deposit forfeiture",
                event.userId(), event.auctionId());
        reputationUseCase.decreaseForAuction(
                event.userId(),
                DEDUCT_REPUTATION_SCORE,
                "Tịch thu cọc đấu giá " + event.auctionId(),
                event.auctionId());
    }
}
