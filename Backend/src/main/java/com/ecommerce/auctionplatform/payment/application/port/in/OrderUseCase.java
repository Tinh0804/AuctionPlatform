package com.ecommerce.auctionplatform.payment.application.port.in;

import com.ecommerce.auctionplatform.payment.application.dto.command.PayEscrowCommand;
import com.ecommerce.auctionplatform.payment.application.dto.command.InitiateOrderPaymentCommand;
import com.ecommerce.auctionplatform.payment.application.dto.command.CompleteOrderCommand;
import com.ecommerce.auctionplatform.payment.application.dto.command.UpdateShippingCommand;
import com.ecommerce.auctionplatform.payment.application.dto.response.OrderPaymentResponse;
import com.ecommerce.auctionplatform.payment.application.dto.response.OrderResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Port/In – Use case interface for Order/Payment domain.
 */
public interface OrderUseCase {

    List<OrderResponse> getMyPurchases();

    List<OrderResponse> getMySales();

    OrderResponse getOrderDetail(UUID orderId);

    OrderPaymentResponse initiateOrderPayment(UUID orderId, InitiateOrderPaymentCommand request);

    void handleGatewayPaymentSuccess(UUID orderId, BigDecimal paidAmount);

    OrderResponse payOrderWithEscrow(UUID orderId, PayEscrowCommand request);

    OrderResponse confirmDeliveryAndReleaseEscrow(UUID orderId);

    OrderResponse updateShippingInfo(UUID orderId, UpdateShippingCommand request);

    OrderResponse completeOrderWithReview(UUID orderId, CompleteOrderCommand request);
}
