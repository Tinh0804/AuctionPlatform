package com.ecommerce.auctionplatform.identity.domain.repository;

import com.ecommerce.auctionplatform.identity.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    List<User> findAllById(Iterable<UUID> ids);
    Optional<User> findByAccountId(UUID accountId);
    Optional<User> findFirstByAccountRoleId(UUID roleId);
    Optional<User> findFirstByAccountRoleName(String roleName);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
}
