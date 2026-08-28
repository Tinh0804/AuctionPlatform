package com.ecommerce.auctionplatform.auction.application.dto.command;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlaceBidCommand {
    BigDecimal bidAmount;
}
