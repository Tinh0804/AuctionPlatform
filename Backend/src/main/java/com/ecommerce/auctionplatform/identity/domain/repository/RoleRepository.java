package com.ecommerce.auctionplatform.identity.domain.repository;

import com.ecommerce.auctionplatform.identity.domain.model.Role;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository {
    Role save(Role role);
    Optional<Role> findById(UUID id);
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
}
