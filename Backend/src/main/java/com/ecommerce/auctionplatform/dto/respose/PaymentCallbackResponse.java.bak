package com.ecommerce.auctionplatform.dto.respose;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentCallbackResponse {
    String orderId;
    String transactionId;
    Long amount;
    String paymentStatus; // "SUCCESS", "FAILED", "PENDING"
    String paymentMethod;
    String message;
    String paymentTime;
}
