package com.ecommerce.auctionplatform.dispute.infrastructure.persistence.mapper;

import com.ecommerce.auctionplatform.dispute.domain.model.Dispute;
import com.ecommerce.auctionplatform.dispute.infrastructure.persistence.entity.DisputeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface DisputePersistenceMapper {
    Dispute toDomain(DisputeEntity entity);
    DisputeEntity toEntity(Dispute domain);
}
