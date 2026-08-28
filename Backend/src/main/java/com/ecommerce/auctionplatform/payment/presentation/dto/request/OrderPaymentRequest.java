package com.ecommerce.auctionplatform.payment.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

public record OrderPaymentRequest(@NotNull String paymentMethod, String pinCode, String returnUrl) {
}
