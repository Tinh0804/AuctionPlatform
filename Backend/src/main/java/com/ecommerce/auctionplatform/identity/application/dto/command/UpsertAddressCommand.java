package com.ecommerce.auctionplatform.identity.application.dto.command;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpsertAddressCommand {
    String ward;

    String district;

    String city;

    String addressLine;

    Boolean isDefault;
}
