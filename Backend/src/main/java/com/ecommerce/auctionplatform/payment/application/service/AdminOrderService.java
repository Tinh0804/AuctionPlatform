package com.ecommerce.auctionplatform.payment.application.service;

import com.ecommerce.auctionplatform.payment.application.dto.response.OrderResponse;
import com.ecommerce.auctionplatform.payment.application.mapper.OrderMapper;
import com.ecommerce.auctionplatform.payment.application.port.in.AdminOrderUseCase;
import com.ecommerce.auctionplatform.payment.application.port.in.OrderUseCase;
import com.ecommerce.auctionplatform.payment.domain.enums.OrderStatus;
import com.ecommerce.auctionplatform.payment.domain.model.Order;
import com.ecommerce.auctionplatform.payment.domain.repository.OrderRepository;
import com.ecommerce.auctionplatform.payment.domain.valueobject.OrderSearchCriteria;
import com.ecommerce.auctionplatform.shared.application.exception.AppException;
import com.ecommerce.auctionplatform.shared.application.exception.ErrorCode;
import com.ecommerce.auctionplatform.shared.application.model.PageQuery;
import com.ecommerce.auctionplatform.shared.domain.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminOrderService implements AdminOrderUseCase {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderUseCase orderUseCase;

    @Override
    @Transactional(readOnly = true)
    public PageResult<OrderResponse> getAllOrders(String status, PageQuery pageQuery) {
        OrderStatus parsedStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                parsedStatus = OrderStatus.valueOf(status.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }
        }
        PageResult<Order> page = orderRepository.search(new OrderSearchCriteria(
                parsedStatus,
                pageQuery.pageNumber(), pageQuery.pageSize(),
                pageQuery.sortBy(), pageQuery.ascending()));
        return new PageResult<>(
                page.items().stream().map(orderMapper::toOrderResponse).toList(),
                page.pageNumber(), page.pageSize(), page.totalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(UUID id) {
        return orderMapper.toOrderResponse(order(id));
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(UUID id) {
        Order order = order(id);
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new AppException(ErrorCode.INVALID_STATUS);
        }
        order.cancel();
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse forcePayOrder(UUID id) {
        Order order = order(id);
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new AppException(ErrorCode.INVALID_STATUS);
        }
        orderUseCase.handleGatewayPaymentSuccess(id, order.getTotalAmount());
        return orderMapper.toOrderResponse(order(id));
    }

    private Order order(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
    }
}
