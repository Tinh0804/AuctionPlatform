package com.ecommerce.auctionplatform.identity.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.identity.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
        Optional<UserEntity> findByAccountId(UUID accountId);
        Optional<UserEntity> findFirstByAccountRoleId(UUID roleId);
        Boolean existsByPhone(String phone);
        Boolean existsByEmail(String email);
        Optional<UserEntity> findFirstByAccount_Role_Name(String roleName);
}
