package com.ecommerce.auctionplatform.dto.respose;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {

    String status;
    String message;
    String paymentUrl;
    String orderId;
    String transactionId;
    Double amount;
    String paymentMethod; // "VNPAY" or "MOMO"
}
