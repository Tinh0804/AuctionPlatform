package com.ecommerce.auctionplatform.product.application.port.in;

import com.ecommerce.auctionplatform.product.application.dto.response.CategoryResponse;
import com.ecommerce.auctionplatform.product.application.dto.command.UpsertCategoryCommand;

import java.util.List;
import java.util.UUID;

public interface CategoryUseCase {
    List<CategoryResponse> getAllCategories();

    CategoryResponse createCategory(UpsertCategoryCommand command);

    CategoryResponse updateCategory(UUID id, UpsertCategoryCommand command);

    void deleteCategory(UUID id);
}
