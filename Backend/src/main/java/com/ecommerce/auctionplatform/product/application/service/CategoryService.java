package com.ecommerce.auctionplatform.product.application.service;

import com.ecommerce.auctionplatform.product.application.dto.response.CategoryResponse;
import com.ecommerce.auctionplatform.product.domain.repository.CategoryRepository;
import com.ecommerce.auctionplatform.product.application.port.in.CategoryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService implements CategoryUseCase {
    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> CategoryResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .description(category.getDescription())
                        .build())
                .collect(Collectors.toList());
    }
}
