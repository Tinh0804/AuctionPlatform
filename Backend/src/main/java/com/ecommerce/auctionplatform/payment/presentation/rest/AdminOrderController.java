package com.ecommerce.auctionplatform.payment.presentation.rest;

import com.ecommerce.auctionplatform.payment.application.port.in.AdminOrderUseCase;
import com.ecommerce.auctionplatform.payment.presentation.dto.response.OrderResponse;
import com.ecommerce.auctionplatform.payment.presentation.mapper.PaymentResponseMapper;
import com.ecommerce.auctionplatform.shared.application.model.PageQuery;
import com.ecommerce.auctionplatform.shared.domain.model.PageResult;
import com.ecommerce.auctionplatform.shared.presentation.response.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {
    private final AdminOrderUseCase adminOrderUseCase;
    private final PaymentResponseMapper responseMapper;

    @GetMapping
    public APIResponse<Page<OrderResponse>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        int pageNumber = Math.max(0, page - 1);
        var result = adminOrderUseCase.getAllOrders(status, new PageQuery(
                        pageNumber, limit, "createdAt", false));
        var pageable = PageRequest.of(result.pageNumber(), result.pageSize());
        Page<OrderResponse> response = new PageImpl<>(
                responseMapper.toOrderResponses(result.items()), pageable, result.totalElements());
        return APIResponse.<Page<OrderResponse>>builder().result(response).build();
    }

    @GetMapping("/{id}")
    public APIResponse<OrderResponse> getOrderDetail(@PathVariable UUID id) {
        return APIResponse.<OrderResponse>builder()
                .result(responseMapper.toOrderResponse(adminOrderUseCase.getOrderDetail(id)))
                .build();
    }

    @PostMapping("/{id}/cancel")
    public APIResponse<OrderResponse> cancelOrder(@PathVariable UUID id) {
        return APIResponse.<OrderResponse>builder()
                .result(responseMapper.toOrderResponse(adminOrderUseCase.cancelOrder(id)))
                .build();
    }

    @PostMapping("/{id}/pay")
    public APIResponse<OrderResponse> forcePayOrder(@PathVariable UUID id) {
        return APIResponse.<OrderResponse>builder()
                .result(responseMapper.toOrderResponse(adminOrderUseCase.forcePayOrder(id)))
                .build();
    }
}
