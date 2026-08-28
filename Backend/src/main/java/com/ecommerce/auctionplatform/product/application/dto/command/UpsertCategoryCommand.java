package com.ecommerce.auctionplatform.product.application.dto.command;

import com.ecommerce.auctionplatform.shared.application.model.FileContent;

import java.util.UUID;

public record UpsertCategoryCommand(
        String name,
        String description,
        UUID parentId,
        FileContent image
) {
}
