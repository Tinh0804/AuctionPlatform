package com.ecommerce.auctionplatform.dto.respose;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressDto {
    UUID id;
    String ward;
    String district;
    String city;
    String addressLine;
    Boolean isDefault;
}
