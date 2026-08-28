package com.ecommerce.auctionplatform.payment.application.mapper;

import com.ecommerce.auctionplatform.payment.application.dto.response.OrderResponse;
import com.ecommerce.auctionplatform.payment.domain.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.ecommerce.auctionplatform.payment.application.port.out.AuctionQueryPort;
import com.ecommerce.auctionplatform.payment.application.port.out.AuctionRecordView;
import com.ecommerce.auctionplatform.payment.application.port.out.UserPort;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderMapper {
    private final AuctionQueryPort auctionQueryPort;
    private final UserPort userPort;

    public OrderResponse toOrderResponse(Order order) {
        AuctionRecordView record = order.getAuctionRecordId() == null
                ? null
                : auctionQueryPort.findRecord(order.getAuctionRecordId()).orElse(null);

        return OrderResponse.builder()
                .id(order.getId())
                .auctionId(record == null ? null : record.auctionId())
                .productName(record == null ? null : record.productName())
                .productImageUrl(record == null ? null : record.productImageUrl())
                .sellerName(userName(order.getSellerId()))
                .buyerName(userName(order.getBuyerId()))
                .totalAmount(order.getTotalAmount())
                .depositAmount(record == null ? null : record.depositAmount())
                .status(order.getStatus())
                .trackingCode(order.getTrackingCode())
                .shippingProvider(order.getShippingProvider())
                .ratingScore(order.getRatingScore())
                .reviewContent(order.getReviewContent())
                .reviewDate(order.getReviewDate())
                .paymentDeadline(record == null ? null : record.paymentDeadline())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private String userName(UUID userId) {
        return userId == null ? null : userPort.findById(userId).map(user -> user.name()).orElse(null);
    }
}
