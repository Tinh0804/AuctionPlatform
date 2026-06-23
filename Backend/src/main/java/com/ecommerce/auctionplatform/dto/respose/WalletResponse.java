package com.ecommerce.auctionplatform.dto.respose;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WalletResponse {
    UUID id;
    
    @JsonProperty("available_balance")
    BigDecimal availableBalance;
    
    @JsonProperty("frozen_balance")
    BigDecimal frozenBalance;
    
    @JsonProperty("has_pin")
    boolean hasPin;
    
    String status;
}
