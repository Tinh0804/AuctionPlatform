package com.ecommerce.auctionplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressRequest {
    @NotBlank(message = "Ward cannot be blank")
    String ward;

    @NotBlank(message = "District cannot be blank")
    String district;

    @NotBlank(message = "City cannot be blank")
    String city;

    @NotBlank(message = "Address line cannot be blank")
    String addressLine;

    Boolean isDefault;
}
