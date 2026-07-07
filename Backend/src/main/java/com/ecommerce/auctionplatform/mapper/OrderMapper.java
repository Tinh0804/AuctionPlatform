package com.ecommerce.auctionplatform.mapper;

import com.ecommerce.auctionplatform.dto.respose.OrderResponse;
import com.ecommerce.auctionplatform.entity.Image;
import com.ecommerce.auctionplatform.entity.Order;
import com.ecommerce.auctionplatform.entity.Product;
import com.ecommerce.auctionplatform.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final ImageRepository imageRepository;

    public OrderResponse toOrderResponse(Order order) {
        String productName = null;
        String productImageUrl = null;
        UUID auctionId = null;
        BigDecimal depositAmount = null;
        LocalDateTime paymentDeadline = null;

        if (order.getAuctionRecord() != null && order.getAuctionRecord().getAuction() != null) {
            var auction = order.getAuctionRecord().getAuction();
            auctionId = auction.getId();
            depositAmount = auction.getDepositAmount();
            if (auction.getProduct() != null) {
                Product product = auction.getProduct();
                productName = product.getName();
                // Fetch cover image
                List<Image> images = imageRepository.findByProductIdOrderByIsCoverDesc(product.getId());
                if (!images.isEmpty()) {
                    productImageUrl = images.get(0).getFileUrl();
                }
            }
            if (order.getAuctionRecord().getExpiryTime() != null) {
                paymentDeadline = order.getAuctionRecord().getExpiryTime();
            }
        }

        return OrderResponse.builder()
                .id(order.getId())
                .auctionId(auctionId)
                .productName(productName)
                .productImageUrl(productImageUrl)
                .sellerName(order.getSeller() != null ? order.getSeller().getName() : null)
                .buyerName(order.getBuyer() != null ? order.getBuyer().getName() : null)
                .totalAmount(order.getTotalAmount())
                .depositAmount(depositAmount)
                .status(order.getStatus())
                .trackingCode(order.getTrackingCode())
                .shippingProvider(order.getShippingProvider())
                .paymentDeadline(paymentDeadline)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
