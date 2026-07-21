package com.ecommerce.auctionplatform.controller;

import com.ecommerce.auctionplatform.dto.respose.APIResponse;
import com.ecommerce.auctionplatform.dto.respose.OrderResponse;
import com.ecommerce.auctionplatform.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminOrderController {

    OrderService orderService;

    @GetMapping
    public APIResponse<Page<OrderResponse>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        
        Page<OrderResponse> orders = orderService.getAllOrdersAdmin(
                status, 
                PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        
        return APIResponse.<Page<OrderResponse>>builder()
                .result(orders)
                .build();
    }
    
    @GetMapping("/{id}")
    public APIResponse<OrderResponse> getOrderDetail(@PathVariable UUID id) {
        return APIResponse.<OrderResponse>builder()
                .result(orderService.getOrderDetail(id))
                .build();
    }

    @PostMapping("/{id}/cancel")
    public APIResponse<OrderResponse> cancelOrder(@PathVariable UUID id) {
        return APIResponse.<OrderResponse>builder()
                .result(orderService.adminCancelOrder(id))
                .build();
    }

    @PostMapping("/{id}/pay")
    public APIResponse<OrderResponse> forcePayOrder(@PathVariable UUID id) {
        return APIResponse.<OrderResponse>builder()
                .result(orderService.adminForcePayOrder(id))
                .build();
    }
}
