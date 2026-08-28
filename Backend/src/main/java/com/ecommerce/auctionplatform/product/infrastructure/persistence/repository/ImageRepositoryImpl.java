package com.ecommerce.auctionplatform.product.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.product.domain.model.Image;
import com.ecommerce.auctionplatform.product.domain.repository.ImageRepository;
import com.ecommerce.auctionplatform.product.infrastructure.persistence.mapper.ProductPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ImageRepositoryImpl implements ImageRepository {
    private final ImageJpaRepository jpaRepository;
    private final ProductPersistenceMapper mapper;

    @Override
    public Image save(Image image) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(image)));
    }

    @Override
    public Optional<Image> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Image> findByProductId(UUID productId) {
        return jpaRepository.findByProductIdOrderBySortOrderAscCreatedAtAsc(productId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Image> findByProductIdOrderByIsCoverDesc(UUID productId) {
        return jpaRepository.findByProductIdOrderByIsCoverDescSortOrderAsc(productId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Image> findFirstByProductIdOrderByIsCoverDesc(UUID productId) {
        return jpaRepository.findFirstByProductIdOrderByIsCoverDesc(productId)
                .map(mapper::toDomain);
    }

    @Override
    public void deleteByProductId(UUID productId) {
        jpaRepository.deleteByProductId(productId);
    }
}
