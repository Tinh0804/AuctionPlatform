package com.ecommerce.auctionplatform.payment.application.service;

import com.ecommerce.auctionplatform.payment.application.port.in.OrderCreationUseCase;
import com.ecommerce.auctionplatform.payment.application.port.out.AuctionQueryPort;
import com.ecommerce.auctionplatform.payment.application.port.out.AuctionRecordView;
import com.ecommerce.auctionplatform.payment.domain.enums.OrderStatus;
import com.ecommerce.auctionplatform.payment.domain.model.Order;
import com.ecommerce.auctionplatform.payment.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCreationService implements OrderCreationUseCase {
    private final AuctionQueryPort auctionQueryPort;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void createForWinner(UUID auctionId, UUID winnerId) {
        AuctionRecordView winnerRecord = auctionQueryPort.findRecord(auctionId, winnerId).orElse(null);
        if (winnerRecord == null) {
            log.error("Could not find winning record for user {} in auction {}", winnerId, auctionId);
            return;
        }
        orderRepository.save(Order.builder()
                .auctionRecordId(winnerRecord.id())
                .sellerId(winnerRecord.sellerId())
                .buyerId(winnerId)
                .totalAmount(winnerRecord.finalPrice())
                .status(OrderStatus.PENDING_PAYMENT)
                .build());
    }
}
