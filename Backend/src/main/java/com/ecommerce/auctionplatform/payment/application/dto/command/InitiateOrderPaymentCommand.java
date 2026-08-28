package com.ecommerce.auctionplatform.payment.application.dto.command;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InitiateOrderPaymentCommand {

    String paymentMethod; // WALLET, MOMO, VNPAY

    // Required for WALLET
    String pinCode;

    // Required for MOMO / VNPAY  
    String returnUrl;
}
