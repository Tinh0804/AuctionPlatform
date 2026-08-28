package com.ecommerce.auctionplatform.auction.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BidRequest(
        @NotNull @Positive @JsonAlias("bid_amount") BigDecimal bidAmount
) {
}
