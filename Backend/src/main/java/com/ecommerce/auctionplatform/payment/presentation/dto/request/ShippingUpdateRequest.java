package com.ecommerce.auctionplatform.payment.presentation.dto.request;

public record ShippingUpdateRequest(String trackingCode, String shippingProvider) {
}
