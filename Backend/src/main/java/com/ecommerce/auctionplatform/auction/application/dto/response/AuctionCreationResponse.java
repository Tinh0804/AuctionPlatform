package com.ecommerce.auctionplatform.auction.application.dto.response;

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
