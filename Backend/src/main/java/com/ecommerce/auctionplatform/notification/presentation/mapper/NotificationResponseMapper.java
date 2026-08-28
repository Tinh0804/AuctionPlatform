package com.ecommerce.auctionplatform.notification.presentation.mapper;

import com.ecommerce.auctionplatform.notification.presentation.dto.response.NotificationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface NotificationResponseMapper {
    NotificationResponse toResponse(
            com.ecommerce.auctionplatform.notification.application.dto.response.NotificationResponse source);

    List<NotificationResponse> toResponses(
            List<com.ecommerce.auctionplatform.notification.application.dto.response.NotificationResponse> source);
}
