package com.ecommerce.auctionplatform.product.domain.model;
import com.ecommerce.auctionplatform.product.domain.enums.ProductCondition;
import com.ecommerce.auctionplatform.product.domain.enums.ProductStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {
    UUID id;

    UUID userId;

    Category category;

    String name;

    ProductCondition condition;

    String description;

    String origin;

    String provenanceFileUrl;

    String manufactureYear;

    @Builder.Default
    Boolean hasCertificate = false;

    @Builder.Default
    ProductStatus status = ProductStatus.PENDING;

    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    LocalDateTime updatedAt;

    public void updateInfo(String name, String description, ProductCondition condition, String origin, String manufactureYear) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (condition != null) this.condition = condition;
        if (origin != null) this.origin = origin;
        if (manufactureYear != null) this.manufactureYear = manufactureYear;
        this.updatedAt = LocalDateTime.now();
    }

    public void approve() {
        this.status = ProductStatus.APPROVED;
        this.updatedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = ProductStatus.REJECTED;
        this.updatedAt = LocalDateTime.now();
    }

}
