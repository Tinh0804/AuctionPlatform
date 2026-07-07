package com.ecommerce.auctionplatform.repository;

import com.ecommerce.auctionplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
        Optional<User> findByAccountId(UUID accountId);
        Optional<User> findFirstByAccountRoleId(UUID roleId);
        Boolean existsByPhone(String phone);
        Boolean existsByEmail(String email);
}
