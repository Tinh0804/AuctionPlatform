package com.ecommerce.auctionplatform.product.domain.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Image {
    UUID id;

    UUID productId;

    String fileUrl;

    @Builder.Default
    Boolean isCover = false;

     // Thứ tự hiển thị.
    @Builder.Default
    Integer sortOrder = 0;

    String description;

    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();
}
