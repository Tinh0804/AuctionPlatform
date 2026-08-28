package com.ecommerce.auctionplatform.product.infrastructure.persistence.mapper;

import com.ecommerce.auctionplatform.product.domain.model.Category;
import com.ecommerce.auctionplatform.product.domain.model.Image;
import com.ecommerce.auctionplatform.product.domain.model.Product;
import com.ecommerce.auctionplatform.product.infrastructure.persistence.entity.CategoryEntity;
import com.ecommerce.auctionplatform.product.infrastructure.persistence.entity.ImageEntity;
import com.ecommerce.auctionplatform.product.infrastructure.persistence.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProductPersistenceMapper {
    Category toDomain(CategoryEntity entity);
    CategoryEntity toEntity(Category domain);

    Product toDomain(ProductEntity entity);
    ProductEntity toEntity(Product domain);

    Image toDomain(ImageEntity entity);
    ImageEntity toEntity(Image domain);
}
