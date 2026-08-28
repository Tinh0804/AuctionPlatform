package com.ecommerce.auctionplatform.product.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

public record CategoryRequest(
        @NotBlank(message = "Tên danh mục không được để trống") String name,
        String description,
        String parentId,
        MultipartFile image
) {
}
