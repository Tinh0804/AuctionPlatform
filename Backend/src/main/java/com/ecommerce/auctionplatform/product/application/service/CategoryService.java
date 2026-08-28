package com.ecommerce.auctionplatform.product.application.service;

import com.ecommerce.auctionplatform.product.application.dto.response.CategoryResponse;
import com.ecommerce.auctionplatform.product.application.dto.command.UpsertCategoryCommand;
import com.ecommerce.auctionplatform.product.domain.model.Category;
import com.ecommerce.auctionplatform.product.domain.repository.CategoryRepository;
import com.ecommerce.auctionplatform.product.domain.repository.ProductRepository;
import com.ecommerce.auctionplatform.product.application.port.in.CategoryUseCase;
import com.ecommerce.auctionplatform.shared.application.exception.AppException;
import com.ecommerce.auctionplatform.shared.application.exception.ErrorCode;
import com.ecommerce.auctionplatform.shared.application.port.out.FileStoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService implements CategoryUseCase {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final FileStoragePort fileStoragePort;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(UpsertCategoryCommand command) {
        Category category = categoryRepository.save(Category.builder()
                .name(command.name())
                .description(command.description())
                .parent(parent(command.parentId()))
                .imageUrl(upload(command))
                .build());
        return toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID id, UpsertCategoryCommand command) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        if (id.equals(command.parentId())) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        category.update(command.name(), command.description(), parent(command.parentId()), upload(command));
        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        if (productRepository.countByCategoryId(id) > 0) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        categoryRepository.deleteById(id);
    }

    private Category parent(UUID parentId) {
        if (parentId == null) return null;
        return categoryRepository.findById(parentId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private String upload(UpsertCategoryCommand command) {
        if (command.image() == null || command.image().bytes().length == 0) return null;
        return fileStoragePort.uploadFile(command.image(), "categories");
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .productCount(productRepository.countByCategoryId(category.getId()))
                .imageUrl(category.getImageUrl())
                .build();
    }
}
