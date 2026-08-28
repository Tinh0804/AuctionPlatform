package com.ecommerce.auctionplatform.identity.application.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressResponse {
    UUID id;
    String ward;
    String district;
    String city;
    String addressLine;
    Boolean isDefault;
}
