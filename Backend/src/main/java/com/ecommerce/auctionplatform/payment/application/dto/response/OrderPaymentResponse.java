package com.ecommerce.auctionplatform.payment.application.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderPaymentResponse {
    String status;        // PAID or PENDING_GATEWAY
    String paymentUrl;    // Only for MOMO/VNPAY
    OrderResponse order;  // Populated after wallet payment
}
