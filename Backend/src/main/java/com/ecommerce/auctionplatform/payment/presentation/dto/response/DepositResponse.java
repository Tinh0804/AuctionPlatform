package com.ecommerce.auctionplatform.payment.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DepositResponse(
        @JsonProperty("payment_url")
        @JsonInclude(JsonInclude.Include.ALWAYS)
        String paymentUrl,
        String message
) {
}
