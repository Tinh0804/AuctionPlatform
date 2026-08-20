package com.ecommerce.auctionplatform.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BidRequest {
    @NotNull(message = "Bid amount cannot be null")
    @Positive(message = "Bid amount must be positive")
    @JsonAlias("bid_amount")
    BigDecimal bidAmount;
}
