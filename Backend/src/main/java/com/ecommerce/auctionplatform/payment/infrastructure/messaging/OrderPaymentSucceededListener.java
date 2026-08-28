package com.ecommerce.auctionplatform.payment.infrastructure.messaging;

import com.ecommerce.auctionplatform.payment.application.port.in.OrderUseCase;
import com.ecommerce.auctionplatform.payment.application.event.OrderPaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderPaymentSucceededListener {
    private final OrderUseCase orderUseCase;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentSucceeded(OrderPaymentSucceededEvent event) {
        orderUseCase.handleGatewayPaymentSuccess(event.orderId(), event.paidAmount());
    }
}
