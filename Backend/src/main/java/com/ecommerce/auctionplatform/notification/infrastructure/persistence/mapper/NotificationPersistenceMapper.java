package com.ecommerce.auctionplatform.notification.infrastructure.persistence.mapper;

import com.ecommerce.auctionplatform.notification.domain.model.Notification;
import com.ecommerce.auctionplatform.notification.infrastructure.persistence.entity.NotificationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface NotificationPersistenceMapper {
    Notification toDomain(NotificationEntity entity);
    NotificationEntity toEntity(Notification domain);
}
