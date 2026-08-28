package com.ecommerce.auctionplatform.identity.application.mapper;

import com.ecommerce.auctionplatform.identity.application.dto.response.AccountResponse;
import com.ecommerce.auctionplatform.identity.domain.enums.PredefinedRole;
import com.ecommerce.auctionplatform.identity.domain.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "role", source = "role", qualifiedByName = "roleToEnum")
    @Mapping(target = "providerType", source = "provider")
    @Mapping(target = "active", source = "isActive")
    @Mapping(target = "email", ignore = true)
    AccountResponse toAccountResponse(Account account);

    @Named("roleToEnum")
    default PredefinedRole roleToEnum(com.ecommerce.auctionplatform.identity.domain.model.Role role) {
        if (role == null || role.getName() == null) return null;
        try {
            return PredefinedRole.valueOf(role.getName());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
