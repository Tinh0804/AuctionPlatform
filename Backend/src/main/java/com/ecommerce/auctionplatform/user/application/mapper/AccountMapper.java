package com.ecommerce.auctionplatform.user.application.mapper;

import com.ecommerce.auctionplatform.user.application.dto.response.AccountResponse;
import com.ecommerce.auctionplatform.user.domain.enums.PredefinedRole;
import com.ecommerce.auctionplatform.user.domain.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "role", source = "role", qualifiedByName = "roleToEnum")
    AccountResponse toAccountResponse(Account account);

    @Named("roleToEnum")
    default PredefinedRole roleToEnum(com.ecommerce.auctionplatform.user.domain.model.Role role) {
        if (role == null || role.getName() == null) return null;
        try {
            return PredefinedRole.valueOf(role.getName());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
