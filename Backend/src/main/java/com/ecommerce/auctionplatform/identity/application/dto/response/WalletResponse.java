package com.ecommerce.auctionplatform.identity.application.dto.response;

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
    
    BigDecimal availableBalance;
    
    BigDecimal frozenBalance;
    
    boolean hasPin;
    
    String status;
}
