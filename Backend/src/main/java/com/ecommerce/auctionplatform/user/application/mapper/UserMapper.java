package com.ecommerce.auctionplatform.user.application.mapper;

import com.ecommerce.auctionplatform.user.application.dto.response.UserResponse;
import com.ecommerce.auctionplatform.user.domain.model.User;
import org.mapstruct.Mapper;
import org.springframework.web.bind.annotation.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @org.mapstruct.Mapping(target = "wallet", ignore = true)
    UserResponse toUserResponse(User user);
}
