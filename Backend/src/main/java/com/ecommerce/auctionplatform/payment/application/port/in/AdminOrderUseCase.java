package com.ecommerce.auctionplatform.payment.application.port.in;

import com.ecommerce.auctionplatform.payment.application.dto.response.OrderResponse;
import com.ecommerce.auctionplatform.shared.application.model.PageQuery;
import com.ecommerce.auctionplatform.shared.domain.model.PageResult;

import java.util.UUID;

public interface AdminOrderUseCase {
    PageResult<OrderResponse> getAllOrders(String status, PageQuery pageQuery);

    OrderResponse getOrderDetail(UUID id);

    OrderResponse cancelOrder(UUID id);

    OrderResponse forcePayOrder(UUID id);
}
