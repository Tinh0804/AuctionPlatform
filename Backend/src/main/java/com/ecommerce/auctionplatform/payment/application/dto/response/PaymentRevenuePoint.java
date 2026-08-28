package com.ecommerce.auctionplatform.payment.application.dto.response;

import java.math.BigDecimal;

public record PaymentRevenuePoint(String date, BigDecimal revenue) {
}
