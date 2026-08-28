package com.ecommerce.auctionplatform.product.presentation.rest;

import com.ecommerce.auctionplatform.product.application.dto.command.UpsertCategoryCommand;
import com.ecommerce.auctionplatform.product.application.port.in.CategoryUseCase;
import com.ecommerce.auctionplatform.product.presentation.dto.request.CategoryRequest;
import com.ecommerce.auctionplatform.product.presentation.dto.response.CategoryResponse;
import com.ecommerce.auctionplatform.product.presentation.mapper.ProductResponseMapper;
import com.ecommerce.auctionplatform.shared.presentation.mapper.FileUploadMapper;
import com.ecommerce.auctionplatform.shared.presentation.response.APIResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {
    private final CategoryUseCase categoryUseCase;
    private final ProductResponseMapper responseMapper;

    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public APIResponse<CategoryResponse> createCategory(@ModelAttribute @Valid CategoryRequest request) {
        return APIResponse.<CategoryResponse>builder()
                .message("Category created successfully")
                .result(responseMapper.toCategoryResponse(categoryUseCase.createCategory(toCommand(request))))
                .build();
    }

    @PutMapping(value = "/{id}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public APIResponse<CategoryResponse> updateCategory(
            @PathVariable UUID id,
            @ModelAttribute @Valid CategoryRequest request
    ) {
        return APIResponse.<CategoryResponse>builder()
                .message("Category updated successfully")
                .result(responseMapper.toCategoryResponse(categoryUseCase.updateCategory(id, toCommand(request))))
                .build();
    }

    @DeleteMapping("/{id}")
    public APIResponse<Void> deleteCategory(@PathVariable UUID id) {
        categoryUseCase.deleteCategory(id);
        return APIResponse.<Void>builder().message("Category deleted successfully").build();
    }

    private UpsertCategoryCommand toCommand(CategoryRequest request) {
        UUID parentId = request.parentId() == null || request.parentId().isBlank()
                ? null
                : UUID.fromString(request.parentId());
        return new UpsertCategoryCommand(
                request.name(),
                request.description(),
                parentId,
                request.image() == null || request.image().isEmpty()
                        ? null
                        : FileUploadMapper.toContent(request.image()));
    }
}
