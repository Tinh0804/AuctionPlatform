package com.ecommerce.auctionplatform.controller;

import com.ecommerce.auctionplatform.dto.request.CategoryRequest;
import com.ecommerce.auctionplatform.dto.respose.APIResponse;
import com.ecommerce.auctionplatform.dto.respose.CategoryResponse;
import com.ecommerce.auctionplatform.entity.enums.PredefinedRole;
import com.ecommerce.auctionplatform.service.CategoryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
public class AdminCategoryController {
    CategoryService categoryService;

    @PostMapping
    public APIResponse<CategoryResponse> createCategory(@RequestBody @Valid CategoryRequest request) {
        return APIResponse.<CategoryResponse>builder()
                .result(categoryService.createCategory(request))
                .message("Category created successfully")
                .build();
    }

    @PutMapping("/{id}")
    public APIResponse<CategoryResponse> updateCategory(
            @PathVariable UUID id,
            @RequestBody @Valid CategoryRequest request) {
        return APIResponse.<CategoryResponse>builder()
                .result(categoryService.updateCategory(id, request))
                .message("Category updated successfully")
                .build();
    }

    @DeleteMapping("/{id}")
    public APIResponse<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return APIResponse.<Void>builder()
                .message("Category deleted successfully")
                .build();
    }
}
