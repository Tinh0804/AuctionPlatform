package com.ecommerce.auctionplatform.identity.infrastructure.persistence.mapper;

import com.ecommerce.auctionplatform.identity.domain.model.Account;
import com.ecommerce.auctionplatform.identity.domain.model.Address;
import com.ecommerce.auctionplatform.identity.domain.model.Role;
import com.ecommerce.auctionplatform.identity.domain.model.User;
import com.ecommerce.auctionplatform.identity.infrastructure.persistence.entity.AccountEntity;
import com.ecommerce.auctionplatform.identity.infrastructure.persistence.entity.AddressEntity;
import com.ecommerce.auctionplatform.identity.infrastructure.persistence.entity.RoleEntity;
import com.ecommerce.auctionplatform.identity.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserPersistenceMapper {
    Role toDomain(RoleEntity entity);
    RoleEntity toEntity(Role domain);

    Account toDomain(AccountEntity entity);
    AccountEntity toEntity(Account domain);

    User toDomain(UserEntity entity);
    UserEntity toEntity(User domain);

    Address toDomain(AddressEntity entity);
    AddressEntity toEntity(Address domain);
}
