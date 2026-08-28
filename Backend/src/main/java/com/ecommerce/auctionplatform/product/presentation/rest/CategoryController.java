package com.ecommerce.auctionplatform.product.presentation.rest;

import com.ecommerce.auctionplatform.product.application.port.in.CategoryUseCase;
import com.ecommerce.auctionplatform.product.presentation.dto.response.CategoryResponse;
import com.ecommerce.auctionplatform.product.presentation.mapper.ProductResponseMapper;
import com.ecommerce.auctionplatform.shared.presentation.response.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auctions/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryUseCase categoryUseCase;
    private final ProductResponseMapper responseMapper;

    @GetMapping
    public APIResponse<List<CategoryResponse>> getCategories() {
        return APIResponse.<List<CategoryResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Categories fetched successfully")
                .result(responseMapper.toCategoryResponses(categoryUseCase.getAllCategories()))
                .build();
    }
}
