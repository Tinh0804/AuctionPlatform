package com.ecommerce.auctionplatform.service;

import com.ecommerce.auctionplatform.dto.request.CategoryRequest;
import com.ecommerce.auctionplatform.dto.respose.CategoryResponse;
import com.ecommerce.auctionplatform.entity.Category;
import com.ecommerce.auctionplatform.exception.AppException;
import com.ecommerce.auctionplatform.exception.ErrorCode;
import com.ecommerce.auctionplatform.repository.CategoryRepository;
import com.ecommerce.auctionplatform.repository.ProductRepository;
import com.ecommerce.auctionplatform.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String imageUrl = null;
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                imageUrl = cloudinaryService.uploadFile(request.getImage(), "categories");
            } catch (Exception e) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(imageUrl)
                .build();
                
        if (request.getParentId() != null && !request.getParentId().isEmpty()) {
            Category parent = categoryRepository.findById(UUID.fromString(request.getParentId()))
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            category.setParent(parent);
        }
        
        return mapToResponse(categoryRepository.save(category));
    }
    
    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
                
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadFile(request.getImage(), "categories");
                category.setImageUrl(imageUrl);
            } catch (Exception e) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }
        }
        
        if (request.getParentId() != null && !request.getParentId().isEmpty()) {
            Category parent = categoryRepository.findById(UUID.fromString(request.getParentId()))
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
        
        return mapToResponse(categoryRepository.save(category));
    }
    
    @Transactional
    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        categoryRepository.delete(category);
    }
    
    private CategoryResponse mapToResponse(Category category) {
        long productCount = productRepository.countByCategoryId(category.getId());
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .productCount(productCount)
                .imageUrl(category.getImageUrl())
                .build();
    }
}
