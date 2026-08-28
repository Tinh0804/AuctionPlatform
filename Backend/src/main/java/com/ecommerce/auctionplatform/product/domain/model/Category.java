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

    String imageUrl;

    public void update(String name, String description, Category parent, String imageUrl) {
        if (name != null && !name.isBlank()) this.name = name;
        this.description = description;
        this.parent = parent;
        if (imageUrl != null) this.imageUrl = imageUrl;
    }
}
