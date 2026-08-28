package com.ecommerce.auctionplatform.product.domain.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Category {
    UUID id;

    String name;

    String description;

    Category parent;
}
