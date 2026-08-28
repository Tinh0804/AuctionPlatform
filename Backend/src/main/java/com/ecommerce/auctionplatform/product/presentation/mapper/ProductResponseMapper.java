package com.ecommerce.auctionplatform.product.presentation.mapper;

import com.ecommerce.auctionplatform.product.presentation.dto.response.CategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProductResponseMapper {
    CategoryResponse toCategoryResponse(
            com.ecommerce.auctionplatform.product.application.dto.response.CategoryResponse source);

    List<CategoryResponse> toCategoryResponses(
            List<com.ecommerce.auctionplatform.product.application.dto.response.CategoryResponse> source);
}
