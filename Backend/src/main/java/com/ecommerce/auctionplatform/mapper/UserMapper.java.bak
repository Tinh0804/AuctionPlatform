package com.ecommerce.auctionplatform.mapper;

import com.ecommerce.auctionplatform.dto.respose.UserResponse;
import com.ecommerce.auctionplatform.entity.User;
import org.mapstruct.Mapper;
import org.springframework.web.bind.annotation.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @org.mapstruct.Mapping(target = "wallet", ignore = true)
    UserResponse toUserResponse(User user);
}
