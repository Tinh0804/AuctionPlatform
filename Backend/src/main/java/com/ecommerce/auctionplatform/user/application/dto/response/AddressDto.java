package com.ecommerce.auctionplatform.user.application.dto.response;

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
