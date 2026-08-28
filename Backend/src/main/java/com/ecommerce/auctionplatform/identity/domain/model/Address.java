package com.ecommerce.auctionplatform.identity.domain.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Address {
    UUID id;

    User user;

    String ward;
    String district;
    String city;

    String addressLine;

    @Builder.Default
    Boolean isDefault = false;

    public void update(String ward, String district, String city, String addressLine, Boolean defaultAddress) {
        this.ward = ward;
        this.district = district;
        this.city = city;
        this.addressLine = addressLine;
        if (defaultAddress != null) {
            this.isDefault = defaultAddress;
        }
    }

    public void clearDefault() {
        isDefault = false;
    }

}
