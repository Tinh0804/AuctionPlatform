package com.ecommerce.auctionplatform.identity.infrastructure.persistence.mapper;

import com.ecommerce.auctionplatform.identity.domain.model.ReputationHistory;
import com.ecommerce.auctionplatform.identity.infrastructure.persistence.entity.ReputationHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ReputationPersistenceMapper {
    ReputationHistory toDomain(ReputationHistoryEntity entity);
    ReputationHistoryEntity toEntity(ReputationHistory domain);
}
