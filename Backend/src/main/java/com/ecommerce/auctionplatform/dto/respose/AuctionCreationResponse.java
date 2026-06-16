package com.ecommerce.auctionplatform.dto.respose;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuctionCreationResponse {
    UUID auctionId;
    String message;
}
