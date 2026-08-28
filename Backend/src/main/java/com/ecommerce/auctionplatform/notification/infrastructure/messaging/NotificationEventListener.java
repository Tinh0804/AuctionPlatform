package com.ecommerce.auctionplatform.notification.infrastructure.messaging;

import com.ecommerce.auctionplatform.auction.application.event.AuctionEndedEvent;
import com.ecommerce.auctionplatform.auction.application.event.AuctionFailedEvent;
import com.ecommerce.auctionplatform.auction.application.event.BidPlacedEvent;
import com.ecommerce.auctionplatform.auction.application.event.WinnerPromotedEvent;
import com.ecommerce.auctionplatform.notification.application.port.in.NotificationUseCase;
import com.ecommerce.auctionplatform.payment.application.event.OrderCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {
    private final NotificationUseCase notificationUseCase;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBidPlaced(BidPlacedEvent event) {
        messagingTemplate.convertAndSend("/topic/auction/" + event.auctionId(), Map.of(
                "type", "NEW_BID",
                "auctionId", event.auctionId(),
                "bidderId", event.bidderId(),
                "bidAmount", event.amount()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAuctionEnded(AuctionEndedEvent event) {
        notificationUseCase.sendNotification(event.winnerId(), "AUCTION_WON", "Bạn đã trúng đấu giá!",
                "Chúc mừng bạn đã trúng đấu giá với mức giá " + event.finalPrice(),
                "AUCTION", event.auctionId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAuctionFailed(AuctionFailedEvent event) {
        log.info("Auction {} failed: {}", event.auctionId(), event.reason());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWinnerPromoted(WinnerPromotedEvent event) {
        notificationUseCase.sendNotification(event.newWinnerId(), "AUCTION_PROMOTED",
                "Bạn được đôn lên thành người thắng!",
                "Người thắng trước đó đã hủy cọc. Bạn có đến " + event.paymentDeadline() + " để thanh toán.",
                "AUCTION", event.auctionId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCompleted(OrderCompletedEvent event) {
        notificationUseCase.sendNotification(event.sellerId(), "ORDER_COMPLETED",
                "Đơn hàng hoàn thành " + "⭐".repeat(event.rating()),
                "Đơn hàng #" + event.orderId().toString().substring(0, 8)
                        + " đã hoàn thành. Số tiền " + String.format("%,.0f", event.netAmount())
                        + "đ đã được giải ngân vào ví.",
                "ORDER", event.orderId());
    }
}
