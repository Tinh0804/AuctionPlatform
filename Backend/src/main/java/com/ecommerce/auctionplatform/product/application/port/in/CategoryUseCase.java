package com.ecommerce.auctionplatform.product.application.port.in;

import com.ecommerce.auctionplatform.product.application.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryUseCase {
    List<CategoryResponse> getAllCategories();
}
