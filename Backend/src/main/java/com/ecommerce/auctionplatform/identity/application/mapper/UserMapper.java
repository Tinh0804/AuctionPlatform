package com.ecommerce.auctionplatform.identity.application.mapper;

import com.ecommerce.auctionplatform.identity.application.dto.response.UserResponse;
import com.ecommerce.auctionplatform.identity.domain.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = AccountMapper.class)
public interface UserMapper {
    @org.mapstruct.Mapping(target = "wallet", ignore = true)
    @org.mapstruct.Mapping(target = "addresses", ignore = true)
    UserResponse toUserResponse(User user);
}
