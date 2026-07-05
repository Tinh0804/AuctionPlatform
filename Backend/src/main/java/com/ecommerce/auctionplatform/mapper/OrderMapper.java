package com.ecommerce.auctionplatform.mapper;

import com.ecommerce.auctionplatform.dto.respose.OrderResponse;
import com.ecommerce.auctionplatform.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface OrderMapper {
    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    @Mapping(source = "buyer", target = "buyer")
    @Mapping(source = "seller", target = "seller")
    OrderResponse toOrderResponse(Order order);
}
